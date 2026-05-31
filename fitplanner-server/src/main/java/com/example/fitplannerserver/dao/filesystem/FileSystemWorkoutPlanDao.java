package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.WorkoutPlanDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.WorkoutPlan;
import com.example.fitplannerserver.model.plan.WorkoutSession;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.example.fitplannerserver.dao.filesystem.CsvUtils.*;

public class FileSystemWorkoutPlanDao implements WorkoutPlanDao {

    private static final String CSV_HEADER="plan_id;title;start_date;cycle_length;assigned_to_id;author_trainer_id;workout_sessions";
    private static final int EXPECTED_COLUMNS = 7;

    private final Path path;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileSystemWorkoutPlanDao(Path path){
        this.path= Objects.requireNonNull(path, "path cannot be null");
        initializeFile(this.path, CSV_HEADER);
    }

    @Override
    public void savePlan(WorkoutPlan plan) throws DaoException {
        Objects.requireNonNull(plan, "plan cannot be null");
        Objects.requireNonNull(plan.getPlanId(), "plan id cannot be null");

        lock.writeLock().lock();
        try{
            CsvUtils.update(path, EXPECTED_COLUMNS, parts->parts[0].equals(plan.getPlanId()), planToCsvRows(plan));
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
            CsvUtils.delete(path, EXPECTED_COLUMNS, parts->parts[0].equals(planId));
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
            List<String[]> risultati = CsvUtils.search(path, EXPECTED_COLUMNS, parts->parts[0].equals(planId), 1);
            if (!risultati.isEmpty()) {
                WorkoutPlan plan = planFromCsvRows(risultati.getFirst());
                return Optional.of(plan);
            }
        }catch (IOException e){
            throw new DaoException("Errore critico durante la ricerca del piano", e);
        }finally {
            lock.readLock().unlock();
        }
        return Optional.empty();
    }

    @Override
    public Optional<WorkoutPlan> findAssignedPlanByAthleteId(String athleteId) throws DaoException {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");

        lock.readLock().lock();
        try{
            List<String[]> risultati = CsvUtils.search(path, EXPECTED_COLUMNS, parts->parts[4].equals(athleteId), 1);
            if (!risultati.isEmpty()) {
                WorkoutPlan plan = planFromCsvRows(risultati.getFirst());
                return Optional.of(plan);
            }
        }catch (IOException e){
            throw new DaoException("Errore critico durante la ricerca del piano", e);
        }finally {
            lock.readLock().unlock();
        }
        return Optional.empty();
    }

    @Override
    public List<WorkoutPlan> findPlansByTrainerId(String trainerId) throws DaoException {
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

        lock.readLock().lock();
        try{
            List<WorkoutPlan> plans = new ArrayList<>();
            List<String[]> rows = CsvUtils.search(path, EXPECTED_COLUMNS, parts->parts[5].equals(trainerId), -1);
                for(String[] row : rows){
                    WorkoutPlan plan = planFromCsvRows(row);
                    plans.add(plan);
            }
                return plans;
        }catch (IOException e){
            throw new DaoException("Errore critico durante la ricerca del piano", e);
        }finally {
            lock.readLock().unlock();
        }
    }

    //HELPER
    private String planToCsvRows(WorkoutPlan plan){
        List<String> workoutSessions = plan.getAllSessions().stream().map(this::sessionsToCsvRows).toList();
        String workoutSessionsString = String.join("|", workoutSessions);

        return String.join(CSV_DELIMITER,
                convertNullToEmptyString(plan.getPlanId()),
                convertNullToEmptyString(plan.getTitle()),
                plan.getStartDate() != null ? plan.getStartDate().toString() : "",
                String.valueOf(plan.getCycleLength()),
                convertNullToEmptyString(plan.getAssignedToId()),
                convertNullToEmptyString(plan.getAuthorId()),
                workoutSessionsString
        );
    }

    private String sessionsToCsvRows(WorkoutSession session){
        return String.join("~",
                convertNullToEmptyString(session.getTitle()),
                convertNullToEmptyString(session.getContent()),
                String.valueOf(session.getDay())
        );
    }

    private WorkoutPlan planFromCsvRows(String[] parts){
        List<WorkoutSession> sessionsList = new java.util.ArrayList<>();
        String sessions = convertEmptyStringToNull(parts[6]);
        if (sessions != null && !sessions.isBlank()) {
            String[] sessionsArray = sessions.split("\\|");
            for (String sessionStr : sessionsArray) {
                sessionsList.add(sessionsFromCsvRows(sessionStr));
            }
        }
        WorkoutPlan workoutPlan= new WorkoutPlan(
                convertEmptyStringToNull(parts[0]),
                convertEmptyStringToNull(parts[1]),
                (parts[3] != null && !parts[3].isEmpty()) ? Integer.parseInt(parts[3]) : 0
        );
        for (WorkoutSession session : sessionsList){
            workoutPlan.addSession(session);
        }
        String startDateStr = convertEmptyStringToNull(parts[2]);
        if (startDateStr != null) {
            workoutPlan.setStartDate(java.time.LocalDate.parse(startDateStr));
        }
        workoutPlan.assignTo(convertEmptyStringToNull(parts[4]));
        workoutPlan.setAuthorId(convertEmptyStringToNull(parts[5]));
        return workoutPlan;
    }

    private WorkoutSession sessionsFromCsvRows(String sessionStr){
        String[] sessionParts = sessionStr.split("~", -1);
        return new WorkoutSession(
                convertEmptyStringToNull(sessionParts[0]),
                convertEmptyStringToNull(sessionParts[1]),
                Integer.parseInt(sessionParts[2])
        );
    }
}
