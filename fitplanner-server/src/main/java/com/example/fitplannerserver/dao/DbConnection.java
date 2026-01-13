package com.example.fitplannerserver.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DbConnection {

    private static final Properties config = new Properties();

    static {
        // Blocco statico: viene eseguito una sola volta quando la classe viene caricata
        try (InputStream input = DbConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new RuntimeException("Impossibile trovare db.properties");
            }
            // Carica le coppie chiave-valore dal file
            config.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Errore durante la lettura di db.properties", ex);
        }

        // Carica il driver (opzionale nelle versioni recenti di Java, ma buona prassi)
//        try {
//            Class.forName("com.my");
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException("Driver PostgreSQL non trovato!", e);
//        }
    }

    // 2. Singleton Thread-Safe
    private static class Wrapper {
        static final DbConnection INSTANCE = new DbConnection(
                config.getProperty("db.url"),
                config.getProperty("db.user"),
                config.getProperty("db.password"),
                Integer.parseInt(config.getProperty("db.pool.size", "10")) // Default 10 se manca
        );
    }

    public static DbConnection getInstance() {
        return Wrapper.INSTANCE;
    }

    private final String url, user, password;
    private final BlockingQueue<Connection> pool;
    private final int maxPoolSize;
    private final AtomicInteger currentPoolSize = new AtomicInteger(0);

    private DbConnection(String url, String user, String password, int maxPoolSize) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        this.pool = new ArrayBlockingQueue<>(maxPoolSize);

        initPool(2); // Iniziamo con poche connessioni
    }

    private void initPool(int initialSize) {
        try {
            for (int i = 0; i < initialSize; i++) {
                pool.offer(createNewConnection());
                currentPoolSize.incrementAndGet();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore critico inizializzazione pool", e);
        }
    }

    private Connection createNewConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public Connection getConnection() throws SQLException, InterruptedException {
        // Prova a prendere una connessione libera subito
        Connection conn = pool.poll();

        if (conn == null) {
            // Se non ce ne sono, controlliamo se possiamo crearne una nuova
            if (currentPoolSize.get() < maxPoolSize) {
                synchronized (this) {
                    // Doppio controllo all'interno del blocco synchronized
                    if (currentPoolSize.get() < maxPoolSize) {
                        Connection newConn = createNewConnection();
                        currentPoolSize.incrementAndGet();
                        return newConn;
                    }
                }
            }
            // Se siamo al limite massimo, aspettiamo che se ne liberi una (blocca per max 5 secondi)
            conn = pool.poll(5, TimeUnit.SECONDS);
            if (conn == null) {
                throw new SQLException("Timeout: Nessuna connessione disponibile nel pool.");
            }
        }

        // Verifica base se la connessione è ancora valida
        if (!conn.isValid(2)) {
            // Se è morta, decrementiamo il contatore e riproviamo (ricorsione semplificata)
            currentPoolSize.decrementAndGet();
            return getConnection();
        }

        return conn;
    }

    public void releaseConnection(Connection conn) {
        if (conn != null) {
            try {
                // Se la connessione è chiusa, non rimetterla nel pool
                if (conn.isClosed()) {
                    currentPoolSize.decrementAndGet();
                } else {
                    // Rimettila in coda
                    boolean inserted = pool.offer(conn);
                    if (!inserted) {
                        // Se il pool è pieno (caso raro/errore), chiudila per evitare leak
                        conn.close();
                        currentPoolSize.decrementAndGet();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}