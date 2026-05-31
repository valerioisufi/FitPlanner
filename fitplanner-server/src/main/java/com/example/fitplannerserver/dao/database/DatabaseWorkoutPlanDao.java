package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.DbConnection;
import com.example.fitplannerserver.dao.WorkoutPlanDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.WorkoutPlan;
import com.example.fitplannerserver.model.plan.WorkoutSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseWorkoutPlanDao implements WorkoutPlanDao {

    private static final String NULL_ATHLETE_ID_MSG = "athleteId cannot be null";
    private static final String NULL_PLAN_ID_MSG="planId cannot be null";
    private static final String NULL_TRAINER_ID_MSG="trainerId cannot be null";
    private static final Logger logger = Logger.getLogger(DatabaseWorkoutPlanDao.class.getName());


    @Override
    public void savePlan(WorkoutPlan plan) throws DaoException {
        Objects.requireNonNull(plan, "plan cannot be null");
        Objects.requireNonNull(plan.getPlanId(), NULL_PLAN_ID_MSG);

        Connection conn= null;

        try{
            conn= DbConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            insertPlan(conn, plan);
            
            try (PreparedStatement delStm = conn.prepareStatement("DELETE FROM workout_session WHERE plan_id=?")) {
                delStm.setString(1, plan.getPlanId());
                delStm.executeUpdate();
            }

            insertWorkoutSession(conn, plan.getPlanId(), plan.getAllSessions());
            conn.commit();
        } catch (SQLException e) {
            DaoException ex = new DaoException("Errore critico durante il salvataggio dei log nel database", e);
            safeRollback(conn, ex);
            throw ex;
        } finally {
            safeRestoreAndRelease(conn);
        }
    }


    @Override
    public void deletePlan(String planId) throws DaoException {
        Objects.requireNonNull(planId, NULL_PLAN_ID_MSG);

        String sql= """
                DELETE FROM workout_plan WHERE plan_id=?
                """;

        Connection conn= null;
        try{
            conn= DbConnection.getInstance().getConnection();
            try(PreparedStatement stm= conn.prepareStatement(sql)){
                stm.setString(1, planId);
                stm.executeUpdate();
            }
        }catch (SQLException e){
            throw new DaoException("Errore critico durante la cancellazione del plan nel database", e);
        }finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Optional<WorkoutPlan> findPlanById(String planId) throws DaoException {
        Objects.requireNonNull(planId, NULL_PLAN_ID_MSG);

        String sql= """
                SELECT WP.plan_id, WP.title as plan_title, WP.cycle_length, WP.start_date, WP.athlete_id, WP.trainer_id, WS.workout_session_id, WS.title AS session_title, WS.content, WS.day
                FROM workout_plan WP LEFT JOIN workout_session WS ON WP.plan_id=WS.plan_id
                WHERE WP.plan_id=?
                ORDER BY WP.start_date DESC, WS.day ASC
                """;
        Connection conn = null;
        try{
            conn = DbConnection.getInstance().getConnection();
            try(PreparedStatement stm= conn.prepareStatement(sql)){
                stm.setString(1, planId);
                try(ResultSet rs = stm.executeQuery()){
                    List<WorkoutPlan> workoutPlans= extractPlan(rs);
                    if(workoutPlans.isEmpty()){
                        return Optional.empty();
                    }
                    return Optional.of(workoutPlans.getFirst());
                }
            }
        }catch (SQLException e){
            throw new DaoException("Errore critico durante la ricerca del plan nel database", e);
        }finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Optional<WorkoutPlan> findAssignedPlanByAthleteId(String athleteId) throws DaoException {
        Objects.requireNonNull(athleteId, NULL_ATHLETE_ID_MSG);

        String sql= """
                SELECT WP.plan_id, WP.title as plan_title, WP.cycle_length, WP.start_date, WP.athlete_id, WP.trainer_id, WS.workout_session_id, WS.title AS session_title, WS.content, WS.day
                FROM workout_plan WP LEFT JOIN workout_session WS ON WP.plan_id=WS.plan_id
                WHERE WP.athlete_id=?
                ORDER BY WP.start_date DESC, WS.day ASC
                """;
        Connection conn= null;
        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, athleteId);
                try (ResultSet rs = stm.executeQuery()) {
                    List<WorkoutPlan> workoutPlans = extractPlan(rs);
                    if (workoutPlans.isEmpty()) {
                        return Optional.empty();
                    }
                    return Optional.of(workoutPlans.getFirst());
                }
            }
        }catch (SQLException e){
            throw new DaoException("Errore critico durante la ricerca del plan nel database", e);
        }finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<WorkoutPlan> findPlansByTrainerId(String trainerId) throws DaoException {
        Objects.requireNonNull(trainerId, NULL_TRAINER_ID_MSG);

        String sql= """
                SELECT WP.plan_id, WP.title as plan_title, WP.cycle_length, WP.start_date, WP.athlete_id, WP.trainer_id, WS.workout_session_id, WS.title AS session_title, WS.content, WS.day
                FROM workout_plan WP LEFT JOIN workout_session WS ON WP.plan_id=WS.plan_id
                WHERE WP.trainer_id=?
                ORDER BY WP.start_date DESC, WS.day ASC
                """;
        Connection conn= null;
        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, trainerId);
                try (ResultSet rs = stm.executeQuery()) {
                    return extractPlan(rs);
                }
            }
        }catch (SQLException e){
            throw new DaoException("Errore critico durante la ricerca del plan nel database", e);
        }finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
    }

    private void insertPlan(Connection conn, WorkoutPlan plan) throws SQLException {
        String sqlPlan="""
                INSERT INTO workout_plan (plan_id, title, cycle_length, start_date, athlete_id, trainer_id) 
                VALUES (?,?,?,?,?,?)
                ON DUPLICATE KEY UPDATE
                title=VALUES(title),
                cycle_length=VALUES(cycle_length),
                start_date=VALUES(start_date),
                athlete_id=VALUES(athlete_id),
                trainer_id=VALUES(trainer_id)
                """;
        try(PreparedStatement stm= conn.prepareStatement(sqlPlan)){
            stm.setString(1, plan.getPlanId());
            stm.setString(2, plan.getTitle());
            stm.setInt(3, plan.getCycleLength());
            stm.setDate(4, plan.getStartDate() == null ? null : java.sql.Date.valueOf(plan.getStartDate()));
            stm.setString(5, plan.getAssignedToId());
            stm.setString(6, plan.getAuthorId());
            stm.executeUpdate();
        }
    }

    private void insertWorkoutSession(Connection conn, String planId, List<WorkoutSession> workoutSessions) throws SQLException {
        String sqlSession="INSERT INTO workout_session (plan_id, title, content, day) VALUES (?,?,?,?)";
        try(PreparedStatement stm= conn.prepareStatement(sqlSession)){
            for(WorkoutSession session : workoutSessions){
                stm.setString(1, planId);
                stm.setString(2, session.getTitle());
                stm.setString(3, session.getContent());
                stm.setInt(4, session.getDay());
                stm.addBatch();
            }
            stm.executeBatch();
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

    private void safeRestoreAndRelease(Connection conn){
        if (conn!= null) {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "Impossibile ripristinare l'auto-commit", e);
            } finally {
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
    }

    private List<WorkoutPlan> extractPlan(ResultSet rs) throws SQLException{
        Map<String, WorkoutPlan> workoutPlanMap= new LinkedHashMap<>();
        while(rs.next()){
            String planId= rs.getString("plan_id");
            workoutPlanMap.computeIfAbsent(planId, k-> {
                try {
                    WorkoutPlan plan= new WorkoutPlan(
                            planId,
                            rs.getString("plan_title"),
                            rs.getInt("cycle_length")
                    );
                    java.sql.Date sqlDate = rs.getDate("start_date");
                    if (sqlDate != null) {
                        plan.setStartDate(sqlDate.toLocalDate());
                    }
                    plan.assignTo(rs.getString("athlete_id"));
                    plan.setAuthorId(rs.getString("trainer_id"));

                    return plan;
                }
                catch (SQLException e){
                    throw new RuntimeException("Errore di mappatura dei dati");
                }
            });
            WorkoutPlan currentPlan= workoutPlanMap.get(planId);
            String workoutSessionId= rs.getString("workout_session_id");
            if(workoutSessionId!=null){
                WorkoutSession currentWorkoutSession= new WorkoutSession(
                        rs.getString("session_title"),
                        rs.getString("content"),
                        rs.getInt("day")
                );
                currentPlan.addSession(currentWorkoutSession);
            }
        }
    return new ArrayList<>(workoutPlanMap.values());
    }

}
