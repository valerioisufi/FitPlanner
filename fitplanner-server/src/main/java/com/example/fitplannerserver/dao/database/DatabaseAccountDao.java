package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.dao.DbConnection;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class DatabaseAccountDao implements AccountDao {

    private static final String nullAccMsg = "account cannot be null";
    private static final String nullEmailMsg = "email cannot be null";
    private static final String nullIdMsg = "userId cannot be null";


    @Override
    public boolean create(Account account) throws DaoException {
        Objects.requireNonNull(account, nullAccMsg);
        Objects.requireNonNull(account.getEmail(), nullEmailMsg);

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
        } catch (SQLException e) {
            if (e instanceof SQLException sqlException && sqlException.getSQLState() != null
                && sqlException.getSQLState().startsWith("23")) {
                return false;
            }
            throw new DaoException("Errore durante la creazione dell'account o di rete", e);
        } finally {
            if (conn != null) {
                DbConnection.getInstance().releaseConnection(conn);
            }

        }
    }

    @Override
    public void save(Account account) throws DaoException {
        Objects.requireNonNull(account, nullAccMsg);
        Objects.requireNonNull(account.getUserId(), nullIdMsg);
        Objects.requireNonNull(account.getEmail(), nullEmailMsg);

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
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante l'aggiornamento dell'account nel database.", e);
        } finally {
            if (conn != null) {
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
    }

    @Override
    public Optional<Account> findByEmail(String email) throws DaoException {
        Objects.requireNonNull(email, nullEmailMsg);

        String sql = "SELECT (user_id, email, password_hash, refreshToken, profileType) FROM accounts WHERE email=?";
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
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la ricerca dell'account nel database", e);
        } finally {
            if (conn != null) {
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

        String sql = "SELECT (user_id, email, password_hash, refreshToken, profileType) FROM accounts WHERE refreshToken=?";
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
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la ricerca dell'account nel database", e);
        } finally {
            if (conn != null) {
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
        return Optional.empty();
    }

    @Override
    public void delete(Account account) throws DaoException {
        Objects.requireNonNull(account, nullEmailMsg);
        Objects.requireNonNull(account.getUserId(), nullIdMsg);

        String sql = "DELETE FROM accounts WHERE user_id=?";
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, account.getUserId());
                stm.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la cancellazione dell'account nel database.", e);
        } finally {
            if (conn != null) {
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
    }

}
