package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.DbConnection;
import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class DatabaseProfileDao implements ProfileDao {

    private static final String INVITATION_CODE="invitation_code";

    @Override
    public Optional<User> findById(String userId) throws DaoException {
        Objects.requireNonNull(userId, "userId cannot be null");

        String sql = "SELECT user_id, first_name, last_name, contact_email, phone_number, invitation_code FROM profiles WHERE user_id=?";
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
                                rs.getString("contact_email"),
                                rs.getString("phone_number"),
                                rs.getString(INVITATION_CODE)
                        );
                        return Optional.of(user);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la ricerca dell'utente nel database", e);
        } finally {
            if (conn != null) {
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
        return Optional.empty();
    }

    @Override
    public void save(User user) throws DaoException {
        Objects.requireNonNull(user, "user cannot be null");

        String sql = """
                INSERT INTO profiles (user_id, first_name, last_name, contact_email, phone_number, invitation_code)
                VALUES (?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                first_name=VALUES(first_name),
                last_name=VALUES(last_name),
                contact_email=VALUES(contact_email),
                phone_number=VALUES(phone_number),
                invitation_code=VALUES(invitation_code);
                """;
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, user.getId());
                stm.setString(2, user.getFirstName());
                stm.setString(3, user.getLastName());
                stm.setString(4, user.getContactEmail());
                stm.setString(5, user.getPhoneNumber());
                stm.setString(6, user.getInvitationCode());
                stm.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante l'aggiornamento (save) dell'utente nel database.", e);
        } finally {
            if (conn != null) {
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
    }

    @Override
    public Optional<User> findByInvitationCode(String invitationCode) throws DaoException {
        if (invitationCode == null || invitationCode.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT user_id, first_name, last_name, contact_email, phone_number, invitation_code FROM profiles WHERE invitation_code=?";
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
                                rs.getString("contact_email"),
                                rs.getString("phone_number"),
                                rs.getString(INVITATION_CODE)
                        );
                        return Optional.of(user);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la ricerca dell'utente nel database", e);
        } finally {
            if (conn != null) {
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
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, userId);
                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next()) {
                        return Optional.ofNullable(rs.getString(INVITATION_CODE));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la ricerca dell'utente nel database", e);
        } finally {
            if (conn != null) {
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
        return Optional.empty();
    }
}
