package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.CoachingDao;
import com.example.fitplannerserver.dao.DbConnection;
import com.example.fitplannerserver.exception.DaoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DatabaseCoachingDao implements CoachingDao {

    @Override
    public void linkAthleteToTrainer(String athleteId, String trainerId) throws DaoException {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

        String sql = "INSERT INTO coaching (trainer, athlete) VALUES (?, ?)";
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, trainerId);
                stm.setString(2, athleteId);
                stm.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante l'aggiunta dell'atleta al proprio trainer nel database", e);
        } finally {
            if (conn != null) {
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
    }

    @Override
    public void unlink(String athleteId, String trainerId) throws DaoException {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

        String sql = """
                DELETE FROM coaching WHERE (trainer=? AND athlete=?);
                """;
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, trainerId);
                stm.setString(2, athleteId);
                stm.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la rimozione dell'atleta dal proprio trainer nel database", e);
        } finally {
            if (conn != null) {
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
    }

    @Override
    public boolean isClientOf(String trainerId, String athleteId) throws DaoException {
        Objects.requireNonNull(trainerId, "trainerId cannot be NULL");
        Objects.requireNonNull(athleteId, "athleteId cannot be NULL");

        String sql = "SELECT * FROM coaching WHERE trainer=? AND athlete=?";
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, trainerId);
                stm.setString(2, athleteId);
                try (ResultSet rs = stm.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la ricerca dell'atleta e/o del trainer nel database", e);
        } finally {
            if (conn != null) {
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
    }

    @Override
    public List<String> findAthleteIdsByTrainerId(String trainerId) throws DaoException {
        Objects.requireNonNull(trainerId, "trainerId cannot be NULL");


        List<String> athleteIds = new java.util.ArrayList<>();
        String sql = "SELECT athlete FROM coaching WHERE trainer=?";
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, trainerId);
                try (ResultSet rs = stm.executeQuery()) {
                    while (rs.next()) {
                        athleteIds.add(rs.getString("athlete"));
                    }
                    return athleteIds;
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la ricerca dell'atleta nel database", e);
        } finally {
            if (conn != null) {
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
    }

    @Override
    public Optional<String> findTrainerIdByAthleteId(String athleteId) throws DaoException {
        Objects.requireNonNull(athleteId, "athleteId cannot be NULL");


        String sql = "SELECT trainer FROM coaching WHERE athlete=?";
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, athleteId);
                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getString("trainer"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la ricerca dell'trainer nel database", e);
        } finally {
            if (conn != null) {
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
        return Optional.empty();
    }
}