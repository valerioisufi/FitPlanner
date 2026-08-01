package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.DbConnection;
import com.example.fitplannerserver.dao.WorkoutPlanDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.WorkoutPlan;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class DatabaseWorkoutPlanDao implements WorkoutPlanDao {

    private static final String NULL_ATHLETE_ID_MSG = "athleteId cannot be null";
    private static final String NULL_PLAN_ID_MSG="planId cannot be null";
    private static final String NULL_TRAINER_ID_MSG="trainerId cannot be null";

    private final DatabaseWorkoutSessionDao workoutSessionDao;

    public DatabaseWorkoutPlanDao(DatabaseWorkoutSessionDao databaseWorkoutSessionDao) {
        this.workoutSessionDao = databaseWorkoutSessionDao;
    }

    @Override
    public void savePlan(WorkoutPlan plan) throws DaoException {
        Objects.requireNonNull(plan, "plan cannot be null");
        Objects.requireNonNull(plan.getPlanId(), NULL_PLAN_ID_MSG);

        Connection conn= null;

        try{
            conn= DbConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            insertPlan(conn, plan);

            workoutSessionDao.deleteSessionsByPlanId(plan.getPlanId());
            workoutSessionDao.saveSessionsForPlan(plan.getPlanId(), plan.getAllSessions());

            conn.commit();
        } catch (SQLException e) {
            DaoException ex = new DaoException("Errore durante il salvataggio del piano", e);
            safeRollback(conn, ex);
            throw ex;
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
    }


    @Override
    public void deletePlan(String planId) throws DaoException {
        Objects.requireNonNull(planId, NULL_PLAN_ID_MSG);

        workoutSessionDao.deleteSessionsByPlanId(planId);

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
                SELECT plan_id, title as plan_title, cycle_length, start_date, athlete_id, trainer_id
                FROM workout_plan
                WHERE plan_id=?
                """;
        Connection conn = null;
        try{
            conn = DbConnection.getInstance().getConnection();
            try(PreparedStatement stm= conn.prepareStatement(sql)){
                stm.setString(1, planId);

                try(ResultSet rs = stm.executeQuery()){
                    List<WorkoutPlan> workoutPlans= extractPlan(rs);
                    if(workoutPlans.isEmpty()) {
                        return Optional.empty();
                    }
                    WorkoutPlan plan = workoutPlans.getFirst();

                    workoutSessionDao.findSessionsByPlanId(plan.getPlanId()).forEach(plan::addSession);
                    return Optional.of(plan);
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
                SELECT plan_id, title as plan_title, cycle_length, start_date, athlete_id, trainer_id
                FROM workout_plan
                WHERE athlete_id=?
                ORDER BY start_date DESC
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
                    WorkoutPlan plan = workoutPlans.getFirst();
                    workoutSessionDao.findSessionsByPlanId(plan.getPlanId()).forEach(plan::addSession);
                    return Optional.of(plan);
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
                SELECT plan_id, title as plan_title, cycle_length, start_date, athlete_id, trainer_id
                FROM workout_plan
                WHERE trainer_id=?
                ORDER BY start_date DESC
                """;
        Connection conn= null;
        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, trainerId);

                try (ResultSet rs = stm.executeQuery()) {
                    List<WorkoutPlan> workoutPlans = extractPlan(rs);

                    for (WorkoutPlan plan : workoutPlans) {
                        workoutSessionDao.findSessionsByPlanId(plan.getPlanId()).forEach(plan::addSession);
                    }
                    return workoutPlans;
                }
            }
        }catch (SQLException e){
            throw new DaoException("Errore critico durante la ricerca del plan nel database", e);
        }finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
    }

    private void insertPlan(Connection conn, WorkoutPlan plan) throws SQLException {
        String sqlPlan = """
                INSERT INTO workout_plan (plan_id, title, cycle_length, start_date, athlete_id, trainer_id)
                VALUES (?,?,?,?,?,?)
                ON DUPLICATE KEY UPDATE
                title=VALUES(title),
                cycle_length=VALUES(cycle_length),
                start_date=VALUES(start_date),
                athlete_id=VALUES(athlete_id),
                trainer_id=VALUES(trainer_id)
                """;
        try (PreparedStatement stm = conn.prepareStatement(sqlPlan)) {
            stm.setString(1, plan.getPlanId());
            stm.setString(2, plan.getTitle());
            stm.setInt(3, plan.getCycleLength());
            stm.setObject(4, plan.getStartDate());
            stm.setString(5, plan.getAssignedToId());
            stm.setString(6, plan.getAuthorId());
            stm.executeUpdate();
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

    private List<WorkoutPlan> extractPlan(ResultSet rs) throws SQLException {
        List<WorkoutPlan> workoutPlans = new ArrayList<>();
        while (rs.next()) {
            WorkoutPlan currentPlan = new WorkoutPlan(
                    rs.getString("plan_id"),
                    rs.getString("plan_title"),
                    rs.getInt("cycle_length")
            );
            LocalDate startDate = rs.getObject("start_date", LocalDate.class);
            if (startDate != null) {
                currentPlan.setStartDate(startDate);
            }
            currentPlan.assignTo(rs.getString("athlete_id"));
            currentPlan.setAuthorId(rs.getString("trainer_id"));

            workoutPlans.add(currentPlan);
        }
        return workoutPlans;
    }

}
