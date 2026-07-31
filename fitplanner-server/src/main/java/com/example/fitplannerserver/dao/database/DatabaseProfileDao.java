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

    private static final String PROFILE_COLUMNS = "SELECT user_id, first_name, last_name, contact_email, phone_number, profile_type, invitation_code, trainer_id";
    private static final String ATHLETE_COLUMNS = "SELECT user_id, first_name, last_name, contact_email, phone_number, trainer_id";
    private static final String TRAINER_COLUMNS = "SELECT user_id, first_name, last_name, contact_email, phone_number, invitation_code";

    @Override
    public Optional<User> findById(String userId) throws DaoException {
        Objects.requireNonNull(userId, "userId cannot be null");

        String sql = PROFILE_COLUMNS + " FROM profiles WHERE user_id=?";
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
                stm.setString(7, user.getInvitationCode());
                stm.setString(8, user.getTrainerId());
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

        String sql = TRAINER_COLUMNS + " FROM profiles WHERE invitation_code=? AND profile_type='TRAINER'";
        Connection conn = null;
        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, invitationCode);

                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapTrainer(rs));
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

        String sql = ATHLETE_COLUMNS + " FROM profiles WHERE trainer_id=? AND profile_type='ATHLETE'";
        Connection conn = null;

        List<AthleteUser> athletes = new ArrayList<>();
        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, trainerId);

                try (ResultSet rs = stm.executeQuery()) {
                    while (rs.next()) {
                        athletes.add(mapAthlete(rs));
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
        Objects.requireNonNull(athleteId, "athleteId cannot be null");

        String sql = ATHLETE_COLUMNS + " FROM profiles WHERE user_id=? AND profile_type='ATHLETE'";
        Connection conn = null;
        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, athleteId);
                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapAthlete(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Errore durante il recupero dell'atleta", e);
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public Optional<TrainerUser> findTrainerById(String trainerId) throws DaoException {
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

        String sql = TRAINER_COLUMNS + " FROM profiles WHERE user_id=? AND profile_type='TRAINER'";
        Connection conn = null;
        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, trainerId);
                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapTrainer(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Errore durante il recupero del trainer", e);
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public void delete(String userId) throws DaoException {
        Objects.requireNonNull(userId, "userId cannot be null");

        String sql = "DELETE FROM profiles WHERE user_id=?";
        Connection conn = null;
        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, userId);
                stm.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException("Errore durante l'eliminazione dell'utente", e);
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        Account.Role type = Account.Role.valueOf(rs.getString("profile_type"));

        if (type == Account.Role.TRAINER) {
            return mapTrainer(rs);
        }
        return mapAthlete(rs);
    }

    private AthleteUser mapAthlete(ResultSet rs) throws SQLException {
        return new AthleteUser(
                rs.getString("user_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("contact_email"),
                rs.getString("phone_number"),
                rs.getString("trainer_id")
        );
    }

    private TrainerUser mapTrainer(ResultSet rs) throws SQLException {
        return new TrainerUser(
                rs.getString("user_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("contact_email"),
                rs.getString("phone_number"),
                rs.getString("invitation_code")
        );
    }
}
