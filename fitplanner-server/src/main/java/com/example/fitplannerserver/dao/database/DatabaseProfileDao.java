package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.DbConnection;
import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.User;

import java.sql.*;
import java.util.Objects;
import java.util.Optional;

public class DatabaseProfileDao implements ProfileDao {

    public DatabaseProfileDao(){
        createTableIfNotExist();
    }

    private void createTableIfNotExist(){
        String sql= """
                CREATE TABLE IF NOT EXISTS profiles(
                user_id VARCHAR(36) PRIMARY KEY,
                first_name VARCHAR(255) NOT NULL,
                last_name VARCHAR(255) NOT NULL,
                email VARCHAR(320),
                phone_number VARCHAR(15),
                invitation_code VARCHAR(255),
                FOREIGN KEY (user_id) REFERENCES accounts(user_id) ON DELETE CASCADE)
                """;
        Connection conn = null;
        try {
            conn = DbConnection.getInstance().getConnection();
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException("Errore SQL: Impossibile creare la tabella 'profiles'.", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore di Connessione: Impossibile inizializzare la tabella 'profiles'.", e);
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Optional<User> findById(String userId) throws DaoException {
        Objects.requireNonNull(userId, "userId cannot be null");

        String sql = "SELECT * FROM profiles WHERE user_id=?";
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, userId);
                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next()) {
                        User user = new User(
                                rs.getString("user_id"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("email"),
                                rs.getString("phone_number"),
                                rs.getString("invitation_code")
                        );
                        return Optional.of(user);
                    }
                }
            }
        } catch (SQLException e){
            throw new DaoException("Errore critico durante la ricerca dell'utente nel database", e);
        } finally {
            if (conn != null){
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
        return Optional.empty();
    }

    @Override
    public void save(User user) throws DaoException {
        Objects.requireNonNull(user, "user cannot be null");

        String sql= """
                UPDATE profiles SET first_name=?, last_name=?, email=?, phone_number=?, invitation_code=? WHERE user_id=?;
                """;
        Connection conn = null;

        try{
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, user.getFirstName());
                stm.setString(2, user.getLastName());
                stm.setString(3, user.getContactEmail());
                stm.setString(4, user.getPhoneNumber());
                stm.setString(5, user.getInvitationCode());
                stm.setString(6, user.getId());
                stm.executeUpdate();
            }
        } catch (SQLException e){
            throw new DaoException("Errore critico durante l'aggiornamento (save) dell'utente nel database.", e);
        } finally {
            if (conn != null){
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
    }

    @Override
    public Optional<User> findByInvitationCode(String invitationCode) throws DaoException {
        if (invitationCode == null || invitationCode.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT * FROM profiles WHERE invitation_code=?";
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, invitationCode);
                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next()) {
                        User user = new User(
                                rs.getString("user_id"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("email"),
                                rs.getString("phone_number"),
                                rs.getString("invitation_code")
                        );
                        return Optional.of(user);
                    }
                }
            }
        }catch (SQLException e){
                throw new DaoException("Errore critico durante la ricerca dell'utente nel database", e);
        }finally {
            if (conn != null){
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> getInvitationCode(String userId) throws DaoException {
        Objects.requireNonNull(userId, "userId cannot be null");

        String sql = """
                SELECT invitation_code FROM profiles WHERE user_id=?;
                """;
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)){
                stm.setString(1, userId);
                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next()) {
                        return Optional.ofNullable(rs.getString("invitation_code"));
                    }
                }
            }
        } catch (SQLException e){
            throw new DaoException("Errore critico durante la ricerca dell'utente nel database", e);
        } finally {
            if (conn != null){
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
        return Optional.empty();
    }
}
