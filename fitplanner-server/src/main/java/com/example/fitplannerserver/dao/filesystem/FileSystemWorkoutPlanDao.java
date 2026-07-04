package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.WorkoutPlanDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.WorkoutPlan;
import com.example.fitplannerserver.model.plan.WorkoutSession;

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

    private static final String CSV_SESSION_HEADER = "plan_id,title,content,day";
    private static final int EXPECTED_SESSION_COLUMNS = 4;

    private final Path plansPath;
    private final Path sessionsPath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileSystemWorkoutPlanDao(Path plansPath, Path sessionsPath){
        this.plansPath = Objects.requireNonNull(plansPath, "plansPath cannot be null");
        this.sessionsPath = Objects.requireNonNull(sessionsPath, "sessionsPath cannot be null");
        initializeFile(this.plansPath, CSV_PLAN_HEADER);
        initializeFile(this.sessionsPath, CSV_SESSION_HEADER);
    }

    @Override
    public void savePlan(WorkoutPlan plan) throws DaoException {
        Objects.requireNonNull(plan, "plan cannot be null");
        Objects.requireNonNull(plan.getPlanId(), "plan id cannot be null");

        lock.writeLock().lock();
        try{
            // upsert della riga del piano, poi si riscrive l'intero insieme delle sue sessioni:
            // si eliminano quelle vecchie e si riappendono quelle correnti
            // (no atomicità dell'operazione)
            CsvUtils.update(plansPath, EXPECTED_PLAN_COLUMNS, parts -> parts[0].equals(plan.getPlanId()), planToCsvRow(plan));

            CsvUtils.delete(sessionsPath, EXPECTED_SESSION_COLUMNS, parts -> parts[0].equals(plan.getPlanId()));
            for (WorkoutSession session : plan.getAllSessions()) {
                CsvUtils.append(sessionsPath, sessionToCsvRow(plan.getPlanId(), session));
            }
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
            CsvUtils.delete(sessionsPath, EXPECTED_SESSION_COLUMNS, parts -> parts[0].equals(planId));
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

    private String sessionToCsvRow(String planId, WorkoutSession session){
        return new CsvRowBuilder()
                .add(planId)
                .add(session.getTitle())
                .add(session.getContent())
                .add(session.getDay())
                .build();
    }

    // ricostruisce il piano dal CsvResultSet e vi aggancia le sue sessioni
    private WorkoutPlan loadPlanWithSessions(CsvResultSet rs) throws IOException {
        WorkoutPlan plan = planFromCsvRS(rs);

        CsvResultSet sessionRs = CsvUtils.search(sessionsPath, EXPECTED_SESSION_COLUMNS,
                parts -> parts[0].equals(plan.getPlanId()), -1);

        while (sessionRs.next()) {
            plan.addSession(sessionFromCsvRS(sessionRs));
        }

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

    private WorkoutSession sessionFromCsvRS(CsvResultSet rs){
        return new WorkoutSession(
                rs.getString(1),
                rs.getString(2),
                rs.getInt(3)
        );
    }
}
