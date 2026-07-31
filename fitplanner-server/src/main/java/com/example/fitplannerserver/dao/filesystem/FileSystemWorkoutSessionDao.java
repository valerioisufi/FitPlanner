package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.WorkoutSessionDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.WorkoutSession;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.example.fitplannerserver.dao.filesystem.CsvUtils.*;

public class FileSystemWorkoutSessionDao implements WorkoutSessionDao {

    private static final String NULL_PLAN_ID_MSG="planId cannot be null";

    private static final String CSV_SESSION_HEADER = "plan_id,title,content,day";
    private static final int EXPECTED_SESSION_COLUMNS = 4;

    private final Path sessionsPath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileSystemWorkoutSessionDao(Path sessionsPath) {
        this.sessionsPath = Objects.requireNonNull(sessionsPath, "sessionsPath cannot be null");
        initializeFile(this.sessionsPath, CSV_SESSION_HEADER);
    }

    @Override
    public void saveSessionsForPlan(String planId, List<WorkoutSession> sessions) throws DaoException {
        Objects.requireNonNull(planId, NULL_PLAN_ID_MSG);

        lock.writeLock().lock();
        try {
            delete(sessionsPath, EXPECTED_SESSION_COLUMNS, parts -> parts[0].equals(planId));
            if (sessions != null) {
                for (WorkoutSession session : sessions) {
                    append(sessionsPath, sessionToCsvRow(planId, session));
                }
            }
        } catch (IOException e) {
            throw new DaoException("Errore durante il salvataggio delle sessioni di allenamento su file system", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<WorkoutSession> findSessionsByPlanId(String planId) throws DaoException {
        Objects.requireNonNull(planId, NULL_PLAN_ID_MSG);

        lock.readLock().lock();
        List<WorkoutSession> list = new ArrayList<>();
        try {
            CsvResultSet rs = search(sessionsPath, EXPECTED_SESSION_COLUMNS, parts -> parts[0].equals(planId), -1);
            while (rs.next()) {
                list.add(new WorkoutSession(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getInt(3)
                ));
            }

            list.sort(Comparator.comparingInt(WorkoutSession::getDay));
            return list;
        } catch (IOException e) {
            throw new DaoException("Errore durante il recupero delle sessioni per plan_id: " + planId, e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<WorkoutSession> findSessionByPlanIdAndDay(String planId, int day) throws DaoException {
        Objects.requireNonNull(planId, NULL_PLAN_ID_MSG);

        lock.readLock().lock();
        try {
            CsvResultSet rs = search(sessionsPath, EXPECTED_SESSION_COLUMNS, parts -> parts[0].equals(planId) && Integer.parseInt(parts[3]) == day, 1);
            if (rs.next()) {
                return Optional.of(new WorkoutSession(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getInt(3)
                ));
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new DaoException("Errore durante la ricerca della sessione per giorno", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void deleteSessionsByPlanId(String planId) throws DaoException {
        Objects.requireNonNull(planId, NULL_PLAN_ID_MSG);

        lock.writeLock().lock();
        try {
            delete(sessionsPath, EXPECTED_SESSION_COLUMNS, parts -> parts[0].equals(planId));
        } catch (IOException e) {
            throw new DaoException("Errore durante la cancellazione delle sessioni", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private String sessionToCsvRow(String planId, WorkoutSession s) {
        return new CsvRowBuilder()
                .add(planId)
                .add(s.getTitle())
                .add(s.getContent())
                .add(s.getDay())
                .build();
    }
}
