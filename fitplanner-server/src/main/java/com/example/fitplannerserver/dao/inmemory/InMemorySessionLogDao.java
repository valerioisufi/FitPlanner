package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.SessionLogDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.log.ExerciseLog;
import com.example.fitplannerserver.model.log.SessionLog;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemorySessionLogDao implements SessionLogDao {

    private static class Wrapper {
        public static final InMemorySessionLogDao INSTANCE = new InMemorySessionLogDao();
    }

    private InMemorySessionLogDao(){}

    public static InMemorySessionLogDao getInstance() {
        return InMemorySessionLogDao.Wrapper.INSTANCE;
    }

    // Map Key: athleteUuid (userId)
    private final Map<String, List<SessionLog>> sessionLogs = new ConcurrentHashMap<>();

    @Override
    public void saveSessionLog(SessionLog log) throws DaoException {
        Objects.requireNonNull(log, "sessionLog cannot be null");
        Objects.requireNonNull(log.getUserId(), "userId cannot be null");
        Objects.requireNonNull(log.getDate(), "session date cannot be null");

        String athleteId = log.getUserId();

        LocalDateTime truncatedDate = log.getDate().truncatedTo(ChronoUnit.SECONDS);
        log.setDate(truncatedDate);

        // Deep copy before storing into the database
        SessionLog copyOfLog = new SessionLog(log);

        sessionLogs.compute(athleteId, (key, logs) -> {
            if (logs == null) {
                logs = new CopyOnWriteArrayList<>();
            } else {
                // If a log with the exact same Date (down to the second) exists, remove it.
                logs.removeIf(existingLog ->
                        existingLog.getDate() != null &&
                                existingLog.getDate().truncatedTo(ChronoUnit.SECONDS).isEqual(truncatedDate)
                );
            }
            logs.add(copyOfLog);
            return logs;
        });
    }

    @Override
    public List<SessionLog> findLogsByAthleteIdAndDateRange(String athleteId, long startTimestamp, long endTimestamp) {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");

        LocalDateTime startDate = LocalDateTime.ofEpochSecond(startTimestamp / 1000, 0, ZoneOffset.UTC);
        LocalDateTime endDate = LocalDateTime.ofEpochSecond(endTimestamp / 1000, 0, ZoneOffset.UTC);

        List<SessionLog> logs = sessionLogs.get(athleteId);
        if (logs == null) return List.of();

        return logs.stream()
                .filter(log -> {
                    LocalDateTime logDate = log.getDate();
                    if (logDate == null) return false;

                    return !logDate.isBefore(startDate) && !logDate.isAfter(endDate);
                })
                .map(SessionLog::new)
                .toList();
    }

    @Override
    public Optional<SessionLog> findMostRecentSessionContainingExercise(String athleteId, String exerciseUuid) {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");
        Objects.requireNonNull(exerciseUuid, "exerciseUuid cannot be null");

        List<SessionLog> logs = sessionLogs.get(athleteId);

        if (logs == null || logs.isEmpty()) {
            return Optional.empty();
        }

        return logs.stream()
                .filter(log -> containsExercise(log, exerciseUuid))
                .max(Comparator.comparing(SessionLog::getDate))
                .map(SessionLog::new);

    }

    private boolean containsExercise(SessionLog session, String targetExerciseUuid) {
        if (session.getExerciseLogs() == null) return false;

        for (ExerciseLog exLog : session.getExerciseLogs()) {
            if (targetExerciseUuid.equals(exLog.getExerciseId())) {
                return true;
            }
        }
        return false;
    }
}
