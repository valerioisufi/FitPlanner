package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.DbConnection;
import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.user.Account;
import com.example.fitplannerserver.model.user.AthleteUser;
import com.example.fitplannerserver.model.user.TrainerUser;
import com.example.fitplannerserver.model.user.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DatabaseProfileDao implements ProfileDao {

    @Override
    public Optional<User> findById(String userId) throws DaoException {
        Objects.requireNonNull(userId, "userId cannot be null");

        String sql = "SELECT user_id, first_name, last_name, contact_email, phone_number, profile_type, invitation_code, trainer_id FROM profiles WHERE user_id=?";
        Connection conn = null;
        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, userId);

                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapUser(rs));
                    }
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la ricerca dell'utente nel database", e);
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public void save(User user) throws DaoException {
        Objects.requireNonNull(user, "user cannot be null");

        String sql = """
                INSERT INTO profiles (user_id, first_name, last_name, contact_email, phone_number, profile_type, invitation_code, trainer_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                first_name=VALUES(first_name),
                last_name=VALUES(last_name),
                contact_email=VALUES(contact_email),
                phone_number=VALUES(phone_number),
                profile_type=VALUES(profile_type),
                invitation_code=VALUES(invitation_code),
                trainer_id=VALUES(trainer_id);
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
                stm.setString(6, user.getProfileType().name());
                stm.setString(7, user instanceof TrainerUser t ? t.getInvitationCode() : null);
                stm.setString(8, user instanceof AthleteUser a ? a.getTrainerId() : null);
                stm.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante l'aggiornamento (save) dell'utente nel database.", e);
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Optional<TrainerUser> findByInvitationCode(String invitationCode) throws DaoException {
        if (invitationCode == null || invitationCode.isBlank()) {
            return Optional.empty();
        }

        String sql = "SELECT user_id, first_name, last_name, contact_email, phone_number, profile_type, invitation_code, trainer_id FROM profiles WHERE invitation_code=?";
        Connection conn = null;
        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, invitationCode);

                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next() && mapUser(rs) instanceof TrainerUser trainer) {
                        return Optional.of(trainer);
                    }
                }
            }

        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la ricerca dell'utente nel database", e);
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<AthleteUser> findAthletesByTrainerId(String trainerId) throws DaoException {
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

        String sql = "SELECT user_id, first_name, last_name, contact_email, phone_number, profile_type, invitation_code, trainer_id FROM profiles WHERE trainer_id=?";
        Connection conn = null;

        List<AthleteUser> athletes = new ArrayList<>();
        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, trainerId);

                try (ResultSet rs = stm.executeQuery()) {
                    while (rs.next()) {
                        if (mapUser(rs) instanceof AthleteUser athlete) {
                            athletes.add(athlete);
                        }
                    }

                }
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la ricerca degli atleti nel database", e);
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
        return athletes;
    }

    @Override
    public Optional<AthleteUser> findAthleteById(String athleteId) throws DaoException {
        return findById(athleteId).filter(AthleteUser.class::isInstance).map(AthleteUser.class::cast);
    }

    @Override
    public Optional<TrainerUser> findTrainerById(String trainerId) throws DaoException {
        return findById(trainerId).filter(TrainerUser.class::isInstance).map(TrainerUser.class::cast);
    }

    private User mapUser(ResultSet rs) throws SQLException {
        Account.Role type = Account.Role.valueOf(rs.getString("profile_type"));

        if (type == Account.Role.TRAINER) {
            return new TrainerUser(
                    rs.getString("user_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("contact_email"),
                    rs.getString("phone_number"),
                    rs.getString("invitation_code")
            );
        }

        return new AthleteUser(
                rs.getString("user_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("contact_email"),
                rs.getString("phone_number"),
                rs.getString("trainer_id")
        );
    }

}
