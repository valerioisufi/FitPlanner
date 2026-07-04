package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.config.ServerConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DbConnection {

    private static final Logger logger = LoggerFactory.getLogger(DbConnection.class);

    private static class Wrapper {
        static final DbConnection INSTANCE = new DbConnection(
                ServerConfigurationManager.getInstance().getDbUrl(),
                ServerConfigurationManager.getInstance().getDbUser(),
                ServerConfigurationManager.getInstance().getDbPassword(),
                ServerConfigurationManager.getInstance().getDbPoolSize()
        );
    }

    public static DbConnection getInstance() {
        return Wrapper.INSTANCE;
    }

    private final String url;
    private final String user;
    private final String password;

    private final BlockingQueue<Connection> pool;
    private final int maxPoolSize;
    private final AtomicInteger currentPoolSize = new AtomicInteger(0);

    private DbConnection(String url, String user, String password, int maxPoolSize) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("Il parametro 'db.url' non può essere nullo o vuoto");
        }
        this.url = url;
        this.user = user;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        this.pool = new ArrayBlockingQueue<>(maxPoolSize);

        initPool(1);
    }

    private void initPool(int initialSize) {
        for (int i = 0; i < initialSize; i++) {
            try {
                Connection conn = createNewConnection();
                boolean inserted = pool.offer(conn);
                if (!inserted) {
                    conn.close();
                    return;
                }
                currentPoolSize.incrementAndGet();
            } catch (SQLException e) {
                logger.error("Avviso: Impossibile creare la connessione iniziale. Verrà effettuato un nuovo tentativo alla prima richiesta.", e);
            }
        }
    }

    private Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public Connection getConnection() throws SQLException {
        long startTime = System.currentTimeMillis();
        long timeoutMs = 5000; // 5 secondi totali di timeout

        while (true) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw new SQLException("Timeout: Impossibile ottenere una connessione valida.");
            }

            // provo a prendere una connessione dal pool
            Connection conn = pool.poll();

            if (conn == null) {
                // se vuoto, prova a creare una nuova connessione se non abbiamo raggiunto il limite massimo
                conn = tryCreateConnection();
                if (conn != null) {
                    return conn;
                }

                // se non ho potuto creare una nuova connessione (pool pieno), attendo
                conn = awaitConnectionFromPool(startTime, timeoutMs);
            }

            // validazione
            if (conn.isValid(2)) {
                return conn;
            } else {
                // se la connessione non è valida:
                // decrementiamo, chiudiamo la risorsa reale e il ciclo while ricomincia
                currentPoolSize.decrementAndGet();
                try {
                    conn.close();
                } catch (Exception e) {
                    logger.warn("Impossibile chiudere la connessione invalida: {}", e.getMessage());
                }
            }
        }
    }

    private Connection tryCreateConnection() throws SQLException {
        if (currentPoolSize.get() < maxPoolSize) {

            synchronized (this) {
                if (currentPoolSize.get() < maxPoolSize) {
                    Connection newConn = createNewConnection();
                    currentPoolSize.incrementAndGet();

                    return newConn;
                }
            }
        }
        return null;
    }

    private Connection awaitConnectionFromPool(long startTime, long timeoutMs) throws SQLException {
        long remaining = timeoutMs - (System.currentTimeMillis() - startTime); // tempo residuo per il poll
        if (remaining <= 0) remaining = 1;

        try {
            Connection conn = pool.poll(remaining, TimeUnit.MILLISECONDS);

            if (conn == null) {
                throw new SQLException("Timeout: Nessuna connessione disponibile nel pool.");
            }

            return conn;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Il thread è stato interrotto durante l'attesa di una connessione dal pool.", e);
        }
    }

    public void releaseConnection(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            // se la connessione è chiusa, non rimetterla nel pool
            if (conn.isClosed()) {
                currentPoolSize.decrementAndGet();
                return;
            }

            // il pool deve contenere solo connessioni con auto-commit attivo:
            // se il ripristino fallisce la connessione viene scartata
            if (!restoreAutoCommit(conn)) {
                discardConnection(conn);
                return;
            }

            // rimetto la connessione nel pool
            boolean inserted = pool.offer(conn);
            if (!inserted) {
                // se il pool è pieno, chiudo la connessione
                discardConnection(conn);
            }
        } catch (SQLException e) {
            logger.error("Errore durante il rilascio della connessione", e);
        }
    }

    private boolean restoreAutoCommit(Connection conn) {
        try {
            if (!conn.getAutoCommit()) {
                conn.rollback();
                conn.setAutoCommit(true);
            }
            return true;
        } catch (SQLException e) {
            logger.warn("Impossibile ripristinare l'auto-commit sulla connessione: {}", e.getMessage());
            return false;
        }
    }

    private void discardConnection(Connection conn) {
        currentPoolSize.decrementAndGet();
        try {
            conn.close();
        } catch (Exception e) {
            logger.warn("Impossibile chiudere la connessione scartata: {}", e.getMessage());
        }
    }
}