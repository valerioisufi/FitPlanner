package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.WorkoutPlanDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.WorkoutPlan;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.example.fitplannerserver.dao.filesystem.CsvUtils.*;

public class FileSystemWorkoutPlanDao implements WorkoutPlanDao {

    private static final String CSV_PLAN_HEADER = "plan_id,title,start_date,cycle_length,assigned_to_id,author_trainer_id";
    private static final int EXPECTED_PLAN_COLUMNS = 6;

    private final Path plansPath;
    private final FileSystemWorkoutSessionDao workoutSessionDao;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileSystemWorkoutPlanDao(Path plansPath, FileSystemWorkoutSessionDao workoutSessionDao) {
        this.plansPath = Objects.requireNonNull(plansPath, "plansPath cannot be null");
        this.workoutSessionDao = Objects.requireNonNull(workoutSessionDao, "workoutSessionDao cannot be null");

        initializeFile(this.plansPath, CSV_PLAN_HEADER);
    }

    @Override
    public void savePlan(WorkoutPlan plan) throws DaoException {
        Objects.requireNonNull(plan, "plan cannot be null");
        Objects.requireNonNull(plan.getPlanId(), "plan id cannot be null");

        lock.writeLock().lock();
        try{
            CsvUtils.update(plansPath, EXPECTED_PLAN_COLUMNS, parts -> parts[0].equals(plan.getPlanId()), planToCsvRow(plan));

            workoutSessionDao.saveSessionsForPlan(plan.getPlanId(), plan.getAllSessions());
        } catch (IOException e) {
            throw new DaoException("Errore critico durante il salvataggio del piano", e);
        }finally{
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deletePlan(String planId) throws DaoException {
        Objects.requireNonNull(planId, "planId cannot be null");
        lock.writeLock().lock();
        try{
            CsvUtils.delete(plansPath, EXPECTED_PLAN_COLUMNS, parts -> parts[0].equals(planId));
            workoutSessionDao.deleteSessionsByPlanId(planId);
        } catch (IOException e) {
            throw new DaoException("Errore critico durante l'eliminazione del piano", e);
        }finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<WorkoutPlan> findPlanById(String planId) throws DaoException {
        Objects.requireNonNull(planId, "planID cannot be null");

        lock.readLock().lock();
        try{
            CsvResultSet rs = CsvUtils.search(plansPath, EXPECTED_PLAN_COLUMNS, parts -> parts[0].equals(planId), 1);
            if (rs.next()) {
                return Optional.of(loadPlanWithSessions(rs));
            }
            return Optional.empty();
        }catch (IOException e){
            throw new DaoException("Errore critico durante la ricerca del piano", e);
        }finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<WorkoutPlan> findAssignedPlanByAthleteId(String athleteId) throws DaoException {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");

        lock.readLock().lock();
        try{
            CsvResultSet rs = CsvUtils.search(plansPath, EXPECTED_PLAN_COLUMNS, parts -> parts[4].equals(athleteId), 1);
            if (rs.next()) {
                return Optional.of(loadPlanWithSessions(rs));
            }
            return Optional.empty();
        }catch (IOException e){
            throw new DaoException("Errore critico durante la ricerca del piano", e);
        }finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<WorkoutPlan> findPlansByTrainerId(String trainerId) throws DaoException {
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

        lock.readLock().lock();
        try{
            List<WorkoutPlan> plans = new ArrayList<>();
            CsvResultSet rs = CsvUtils.search(plansPath, EXPECTED_PLAN_COLUMNS, parts -> parts[5].equals(trainerId), -1);
            while (rs.next()) {
                plans.add(loadPlanWithSessions(rs));
            }
            return plans;
        }catch (IOException e){
            throw new DaoException("Errore critico durante la ricerca del piano", e);
        }finally {
            lock.readLock().unlock();
        }
    }

    //HELPER
    private String planToCsvRow(WorkoutPlan plan){
        return new CsvRowBuilder()
                .add(plan.getPlanId())
                .add(plan.getTitle())
                .add(plan.getStartDate() != null ? plan.getStartDate().toString() : null)
                .add(plan.getCycleLength())
                .add(plan.getAssignedToId())
                .add(plan.getAuthorId())
                .build();
    }

    // ricostruisce il piano dal CsvResultSet e vi aggancia le sue sessioni
    private WorkoutPlan loadPlanWithSessions(CsvResultSet rs) throws DaoException {
        WorkoutPlan plan = planFromCsvRS(rs);
        workoutSessionDao.findSessionsByPlanId(plan.getPlanId()).forEach(plan::addSession);

        return plan;
    }

    private WorkoutPlan planFromCsvRS(CsvResultSet rs){
        WorkoutPlan plan = new WorkoutPlan(rs.getString(0), rs.getString(1), rs.getInt(3));

        String startDateStr = rs.getString(2);
        if (startDateStr != null) {
            plan.setStartDate(LocalDate.parse(startDateStr));
        }
        plan.assignTo(rs.getString(4));
        plan.setAuthorId(rs.getString(5));
        return plan;
    }

}
