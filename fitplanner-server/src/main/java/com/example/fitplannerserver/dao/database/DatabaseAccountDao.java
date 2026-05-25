package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.dao.DbConnection;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.Account;


import java.sql.*;
import java.util.Objects;
import java.util.Optional;

public class    DatabaseAccountDao implements AccountDao {

    public DatabaseAccountDao(){
        createTableIfNotExist();
    }

    private void createTableIfNotExist(){
        String sql="""
                CREATE TABLE IF NOT EXISTS accounts(
                user_id VARCHAR(36) PRIMARY KEY,
                email VARCHAR(320) NOT NULL UNIQUE,
                password_hash VARCHAR(255) NOT NULL,
                refreshToken VARCHAR(255),
                profile_type VARCHAR(50) NOT NULL);
                """;
        Connection conn= null;
        try{
            conn = DbConnection.getInstance().getConnection();
            try (Statement stm= conn.createStatement()) {
                stm.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException("Errore SQL: Impossibile creare la tabella 'accounts'. " +
                        "Verifica la query o i permessi utente su MySQL.", e);
            }
        } catch (SQLException | InterruptedException e) {
                throw new RuntimeException("Errore critico: impossibile inizializzare la tabella 'accounts'. " +
                        "Il database è irraggiungibile o i permessi sono errati.", e);
        } finally {
            if (conn != null){
                DbConnection.getInstance().releaseConnection(conn);
                }
        }
    }

    @Override
    public boolean create(Account account) throws DaoException {
        Objects.requireNonNull(account, "Account cannot be null");
        Objects.requireNonNull(account.getEmail(), "Account email cannot be null");

        String sql = "INSERT INTO accounts(user_id, email, password_hash, refreshToken, profileType) VALUES (?,?,?,?,?)";
        Connection conn = null;
        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, account.getUserId());
                stm.setString(2, account.getEmail().toLowerCase());
                stm.setString(3, account.getPasswordHash());
                stm.setString(4, account.getRefreshToken());
                stm.setString(5, account.getProfileType().name());
                int rowsAffected = stm.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException | InterruptedException e) {
            if (e instanceof SQLException sqlException && sqlException.getSQLState() != null
                    && sqlException.getSQLState().startsWith("23")) {
                return false;
            }
            throw new DaoException("Errore durante la creazione dell'account o di rete", e);
        } finally {
            if (conn != null){
                DbConnection.getInstance().releaseConnection(conn);
            }

        }
    }

    @Override
    public void save(Account account) throws DaoException {
        Objects.requireNonNull(account, "Account cannot be null");
        Objects.requireNonNull(account.getUserId(), "Account userId cannot be null");
        Objects.requireNonNull(account.getEmail(), "Account email cannot be null");

        String sql = """
                UPDATE accounts SET email=?, password_hash=?, refreshToken=?, profileType=? WHERE user_id = ?;
                """;
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, account.getEmail().toLowerCase());
                stm.setString(2, account.getPasswordHash());
                stm.setString(3, account.getRefreshToken());
                stm.setString(4, account.getProfileType().name());
                stm.setString(5, account.getUserId());
                stm.executeUpdate();
            }
        } catch (SQLException | InterruptedException e) {
            throw new DaoException("Errore critico durante l'aggiornamento (save) dell'account nel database.", e);
        } finally {
            if (conn != null){
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
    }

    @Override
    public Optional<Account> findByEmail(String email) throws DaoException {
        Objects.requireNonNull(email, "Email cannot be null" );

        String sql = "SELECT * FROM accounts WHERE email=?";
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, email.toLowerCase());
                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next()) {
                        Account account = new Account(
                                rs.getString("user_id"),
                                rs.getString("email"),
                                rs.getString("password_hash"),
                                rs.getString("refreshToken"),
                                Account.Role.valueOf(rs.getString("profileType"))
                        );
                        return Optional.of(account);
                    }
                }
            }
        } catch (SQLException | InterruptedException e){
            throw new DaoException("Errore critico durante la ricerca dell'account nel database", e);
        } finally {
            if (conn != null){
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Account> findByRefreshToken(String refreshToken) throws DaoException {
        if (refreshToken == null || refreshToken.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM accounts WHERE refreshToken=?";
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, refreshToken);
                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next()) {
                        Account account = new Account(
                                rs.getString("user_id"),
                                rs.getString("email"),
                                rs.getString("password_hash"),
                                rs.getString("refreshToken"),
                                Account.Role.valueOf(rs.getString("profileType"))
                        );
                        return Optional.of(account);
                    }
                }
            }
        }catch (SQLException | InterruptedException e){
                throw new DaoException("Errore critico durante la ricerca dell'account nel database", e);
        } finally {
            if (conn != null){
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
        return Optional.empty();
    }

    @Override
    public void delete(Account account) throws DaoException {
        Objects.requireNonNull(account, "Account cannot be null");
        Objects.requireNonNull(account.getUserId(), "Account userId cannot be null");

        String sql = "DELETE FROM accounts WHERE user_id=?";
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, account.getUserId());
                stm.executeUpdate();
            }
        } catch (SQLException | InterruptedException e) {
            throw new DaoException("Errore critico durante la cancellazione dell'account nel database.", e);
        } finally {
            if (conn != null){
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
    }

}
