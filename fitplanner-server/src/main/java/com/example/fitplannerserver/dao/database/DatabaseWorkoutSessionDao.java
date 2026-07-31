package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.DbConnection;
import com.example.fitplannerserver.dao.WorkoutSessionDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.WorkoutSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DatabaseWorkoutSessionDao implements WorkoutSessionDao {

    private static final String NULL_PLAN_ID_MSG="planId cannot be null";

    @Override
    public void saveSessionsForPlan(String planId, List<WorkoutSession> sessions) throws DaoException {
        Objects.requireNonNull(planId, NULL_PLAN_ID_MSG);
        if (sessions == null || sessions.isEmpty()) return;

        String insertSql = "INSERT INTO workout_session (plan_id, day, title, content) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = DbConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement stm = conn.prepareStatement(insertSql)) {
                stm.setString(1, planId);
                for (WorkoutSession session : sessions) {
                    stm.setInt(2, session.getDay());
                    stm.setString(3, session.getTitle());
                    stm.setString(4, session.getContent());
                    stm.addBatch();
                }
                stm.executeBatch();
                conn.commit();
            }
        } catch (SQLException e) {
            DaoException ex = new DaoException("Errore durante il salvataggio delle sessioni di allenamento nel database", e);
            safeRollback(conn, ex);
            throw ex;
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<WorkoutSession> findSessionsByPlanId(String planId) throws DaoException {
        Objects.requireNonNull(planId, NULL_PLAN_ID_MSG);

        String sql = "SELECT title, content, day FROM workout_session WHERE plan_id=? ORDER BY day";
        Connection conn = null;
        List<WorkoutSession> sessions = new ArrayList<>();
        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, planId);
                try (ResultSet rs = stm.executeQuery()) {
                    while (rs.next()) {
                        sessions.add(new WorkoutSession(
                                rs.getString("title"),
                                rs.getString("content"),
                                rs.getInt("day")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Errore durante il recupero delle sessioni per plan_id: " + planId, e);
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
        return sessions;
    }

    @Override
    public Optional<WorkoutSession> findSessionByPlanIdAndDay(String planId, int day) throws DaoException {
        Objects.requireNonNull(planId, NULL_PLAN_ID_MSG);

        String sql = "SELECT title, content, day FROM workout_session WHERE plan_id=? AND day=?";
        Connection conn = null;
        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, planId);
                stm.setInt(2, day);
                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new WorkoutSession(
                                rs.getString("title"),
                                rs.getString("content"),
                                rs.getInt("day")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Errore durante il recupero della sessione nel database", e);
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public void deleteSessionsByPlanId(String planId) throws DaoException {
        Objects.requireNonNull(planId, NULL_PLAN_ID_MSG);

        String sql = "DELETE FROM workout_session WHERE plan_id=?";
        Connection conn = null;
        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, planId);
                stm.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException("Errore durante l'eliminazione delle sessioni nel database", e);
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
    }

    private void safeRollback(Connection conn, DaoException ex){
        if(conn!=null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                ex.addSuppressed(new DaoException("Impossibile eseguire il rollback", e));
            }
        }
    }
}
