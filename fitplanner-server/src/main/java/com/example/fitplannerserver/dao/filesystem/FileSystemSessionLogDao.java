package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.SessionLogDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.log.ExerciseLog;
import com.example.fitplannerserver.model.log.SessionLog;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.example.fitplannerserver.dao.filesystem.CsvUtils.*;

public class FileSystemSessionLogDao implements SessionLogDao {

    private static final String CSV_SESSION_LOG_HEADER = "userId,notes,status,date,planId,workoutSessionDay";
    private static final int EXPECTED_SESSION_LOG_COLUMNS = 6;

    private static final String CSV_EXERCISE_LOG_HEADER = "userId,sessionDate,name,exerciseId,sets,rpe,notes";
    private static final int EXPECTED_EXERCISE_LOG_COLUMNS = 7;

    private final Path sessionLogsPath;
    private final Path exerciseLogsPath;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileSystemSessionLogDao(Path sessionLogsPath, Path exerciseLogsPath){
        this.sessionLogsPath = Objects.requireNonNull(sessionLogsPath, "sessionLogsPath cannot be null");
        this.exerciseLogsPath = Objects.requireNonNull(exerciseLogsPath, "exerciseLogsPath cannot be null");
        initializeFile(this.sessionLogsPath, CSV_SESSION_LOG_HEADER);
        initializeFile(this.exerciseLogsPath, CSV_EXERCISE_LOG_HEADER);
    }

    @Override
    public void saveSessionLog(SessionLog log) throws DaoException {
        Objects.requireNonNull(log, "log cannot be null");
        Objects.requireNonNull(log.getUserId(), "userId cannot be null");
        Objects.requireNonNull(log.getDate(), "date cannot be null");

        lock.writeLock().lock();
        try{
            boolean exists = !CsvUtils.search(sessionLogsPath, EXPECTED_SESSION_LOG_COLUMNS,
                    parts -> parts[0].equals(log.getUserId()) && parts[3].equals(log.getDate().toString()), 1).isEmpty();
            if (exists) {
                throw new DaoException("Esiste già un log per questo utente in questa data");
            }

            CsvUtils.append(sessionLogsPath, sessionLogToCsvRow(log));
            for (ExerciseLog exerciseLog : log.getExerciseLogs()) { // non viene garantita l'atomicità dell'operazione
                CsvUtils.append(exerciseLogsPath, exerciseLogToCsvRow(log.getUserId(), log.getDate().toString(), exerciseLog));
            }

        } catch (IOException e) {
            throw new DaoException("Errore durante il salvataggio della sessione", e);
        }finally {
            lock.writeLock().unlock();

        }

    }

    @Override
    public List<SessionLog> findLogsByAthleteIdAndDateRange(String athleteId, long startTimestamp, long endTimestamp) throws DaoException {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");

        // Convertiamo i timestamp in millisecondi in oggetti LocalDateTime
        LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimestamp), ZoneOffset.UTC);
        LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimestamp), ZoneOffset.UTC);

        lock.readLock().lock();
        try {
            List<SessionLog> results = new ArrayList<>();
            Map<String, SessionLog> sessionMap = new HashMap<>(); // key: sessionDate, value: SessionLog

            CsvResultSet rs = CsvUtils.search(sessionLogsPath, EXPECTED_SESSION_LOG_COLUMNS, parts -> parts[0].equals(athleteId), -1);
            while (rs.next()) {
                SessionLog log = sessionLogFromCsvRS(rs);
                if (!log.getDate().isBefore(start) && !log.getDate().isAfter(end)) {
                    results.add(log);

                    sessionMap.put(log.getDate().toString(), log);
                }
            }

            if (results.isEmpty()) {
                return results;
            }

            // filtriamo cercando tutti gli esercizi dell'atleta in cui la data è presente tra quelle trovate sopra
            CsvResultSet exerciseRS = CsvUtils.search(
                    exerciseLogsPath, EXPECTED_EXERCISE_LOG_COLUMNS,
                    parts -> parts[0].equals(athleteId) && sessionMap.containsKey(parts[1]), -1
            );
            while (exerciseRS.next()) {
                String sessionDate = exerciseRS.getString(1); // recuperiamo la data dell'exerciseLog
                SessionLog targetSession = sessionMap.get(sessionDate); // prendiamo la sessione corrispondente

                if (targetSession != null) {
                    targetSession.addExerciseLog(exerciseLogFromCsvRS(exerciseRS)); // aggiungiamo l'exerciseLog al sessionLog
                }
            }

            return results;

        } catch (IOException e) {
            throw new DaoException("Errore durante la ricerca dei log per range di date", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<SessionLog> findMostRecentSessionContainingExercise(String athleteId, String exerciseId) throws DaoException {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");
        Objects.requireNonNull(exerciseId, "exerciseUuid cannot be null");

        lock.readLock().lock();
        try {
            CsvResultSet rs = CsvUtils.search(exerciseLogsPath, EXPECTED_EXERCISE_LOG_COLUMNS, parts -> parts[0].equals(athleteId) && parts[3].equals(exerciseId), -1);

            List<String> matchingDates = new ArrayList<>();
            while (rs.next()) {
                matchingDates.add(rs.getString(1)); // raccogliamo le date delle sessioni
            }

            // troviamo la data più recente
            String mostRecentDateStr = matchingDates.stream()
                    .max(Comparator.comparing(LocalDateTime::parse))
                    .orElse(null);

            if (mostRecentDateStr == null) {
                return Optional.empty();
            }

            CsvResultSet sessionLogRs = CsvUtils.search(sessionLogsPath, EXPECTED_SESSION_LOG_COLUMNS, parts -> parts[0].equals(athleteId) && parts[3].equals(mostRecentDateStr), -1);
            SessionLog sessionLog;
            if (sessionLogRs.next()) {
                sessionLog = sessionLogFromCsvRS(sessionLogRs);

                // recuperiamo anche gli exerciseLogs
                CsvResultSet exerciseRS = CsvUtils.search(
                        exerciseLogsPath, EXPECTED_EXERCISE_LOG_COLUMNS,
                        parts -> parts[0].equals(athleteId) && parts[1].equals(sessionLog.getDate().toString()), -1
                );

                while (exerciseRS.next()){
                    sessionLog.addExerciseLog(exerciseLogFromCsvRS(exerciseRS));
                }

            } else {
                sessionLog = null;
            }

            return Optional.ofNullable(sessionLog);

        } catch (IOException e) {
            throw new DaoException("Errore durante la ricerca della sessione più recente", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    //HELPER
    private String sessionLogToCsvRow(SessionLog sessionLog) {
        return new CsvUtils.CsvRowBuilder()
                .add(sessionLog.getUserId())
                .add(sessionLog.getNotes())
                .add(sessionLog.getStatus() != null ? sessionLog.getStatus().name() : "")
                .add(sessionLog.getDate() != null ? sessionLog.getDate().toString() : "")
                .add(sessionLog.getPlanId())
                .add(sessionLog.getWorkoutSessionDay())
                .build();

    }

    private String exerciseLogToCsvRow(String userId, String sessionDate, ExerciseLog exerciseLog) {
        // uniamo reps e load con i due punti ":", e i set con ";"
        List<String> sets = exerciseLog.getSets().stream().map(set -> set.reps() + ":" + set.load()).toList();
        String setsString = String.join(";", sets);

        return new CsvUtils.CsvRowBuilder()
                .add(userId)
                .add(sessionDate)
                .add(exerciseLog.getName())
                .add(exerciseLog.getExerciseId())
                .add(setsString)
                .add(exerciseLog.getRpe())
                .add(exerciseLog.getNotes())
                .build();
    }


    private SessionLog sessionLogFromCsvRS(CsvResultSet rs) {
        String userId = rs.getString(0);
        String notes = rs.getString(1);
        SessionLog.SessionStatus status = (rs.getString(2) != null) ? SessionLog.SessionStatus.valueOf(rs.getString(2)) : null;
        LocalDateTime date = (rs.getString(3) != null) ? LocalDateTime.parse(rs.getString(3)) : null;
        String planId = rs.getString(4);
        int day = rs.getInt(5);

        return new SessionLog(userId, notes, status, date, planId, day);
    }

    private ExerciseLog exerciseLogFromCsvRS(CsvResultSet rs) throws DaoException {
        String name = rs.getString(2);
        String exId = rs.getString(3);
        List<ExerciseLog.ExerciseSet> sets = parseExerciseSets(rs.getString(4));
        int rpe = rs.getInt(5);
        String notes = rs.getString(6);

        return new ExerciseLog(name, exId, sets, rpe, notes);
    }

    private List<ExerciseLog.ExerciseSet> parseExerciseSets(String exerciseSetsString) throws DaoException {
        List<ExerciseLog.ExerciseSet> setsList = new ArrayList<>();
        if (exerciseSetsString == null || exerciseSetsString.isBlank()) return setsList;

        String[] setsArray = exerciseSetsString.split(";");
        for (String setStr : setsArray) {
            String[] setParts = setStr.split(":");
            if (setParts.length == 2) {
                try {
                    int reps = Integer.parseInt(setParts[0]);
                    double load = Double.parseDouble(setParts[1]);
                    setsList.add(new ExerciseLog.ExerciseSet(reps, load));
                } catch (NumberFormatException e) {
                    throw new DaoException("Dati corrotti nel set: " + setStr, e);
                }
            }
        }
        return setsList;
    }
}
