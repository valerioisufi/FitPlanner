package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.SessionLogDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.log.ExerciseLog;
import com.example.fitplannerserver.model.log.SessionLog;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.example.fitplannerserver.dao.filesystem.CsvUtils.*;

public class FileSystemSessionLogDao implements SessionLogDao {

    private static final String CSV_SESSION_LOG_HEADER = "userId;notes;status;date;planId;workoutSessionDay;exerciseLogs";
    private static final int EXPECTED_SESSION_LOG_COLUMNS = 7;

    private final Path path;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileSystemSessionLogDao(Path path){
        this.path = Objects.requireNonNull(path, "path cannot be null");
        initializeFile(this.path, CSV_SESSION_LOG_HEADER);
    }

    @Override
    public void saveSessionLog(SessionLog log) throws DaoException {
        Objects.requireNonNull(log, "log cannot be null");
        Objects.requireNonNull(log.getUserId(), "userId cannot be null");
        Objects.requireNonNull(log.getDate(), "date cannot be null");

        lock.writeLock().lock();
        try{
            CsvUtils.update(path, EXPECTED_SESSION_LOG_COLUMNS, parts -> parts[0].equals(log.getUserId()) && parts[3].equals(log.getDate().toString()),
                    sessionLogToCsvRow(log));
            }catch (IOException e) {
            throw new DaoException("Errore durante il salvataggio della sessione", e);
        }finally {
            lock.writeLock().unlock();

        }

    }

    @Override
    public List<SessionLog> findLogsByAthleteIdAndDateRange(String athleteId, long startTimestamp, long endTimestamp) throws DaoException {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");

        // Convertiamo i timestamp SECONDI in oggetti LocalDateTime
        LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochSecond(startTimestamp), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochSecond(endTimestamp), ZoneId.systemDefault());

        lock.readLock().lock();
        try {
            List<SessionLog> results = new ArrayList<>();
            List<String[]> rows = CsvUtils.search(path, EXPECTED_SESSION_LOG_COLUMNS, parts -> parts[0].equals(athleteId), -1);

            for (String[] row : rows) {
                SessionLog log = sessionLogFromCsvRow(row);
                // Filtriamo per range di date
                if (!log.getDate().isBefore(start) && !log.getDate().isAfter(end)) {
                    results.add(log);
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
    public Optional<SessionLog> findMostRecentSessionContainingExercise(String athleteId, String exerciseUuid) throws DaoException {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");
        Objects.requireNonNull(exerciseUuid, "exerciseUuid cannot be null");

        lock.readLock().lock();
        try {
            List<SessionLog> allAthleteLogs = new ArrayList<>();
            List<String[]> rows = CsvUtils.search(path, EXPECTED_SESSION_LOG_COLUMNS, parts -> parts[0].equals(athleteId), -1);

            for (String[] row : rows) {
                allAthleteLogs.add(sessionLogFromCsvRow(row));
            }

            return allAthleteLogs.stream()
                    // Teniamo solo le sessioni che contengono quell'esercizio specifico
                    .filter(log -> log.getExerciseLogs().stream().anyMatch(ex -> exerciseUuid.equals(ex.getExerciseId())))
                    // Ordiniamo per data inversa (il più recente per primo)
                    .max(Comparator.comparing(SessionLog::getDate));

        } catch (IOException e) {
            throw new DaoException("Errore durante la ricerca della sessione più recente", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    //HELPER
    private String sessionLogToCsvRow(SessionLog sessionLog) {
        // L2: Uniamo gli esercizi con il Pipe "|"
        List<String> exerciseLogs = sessionLog.getExerciseLogs().stream().map(this::exerciseLogToCsvRow).toList();
        String exerciseLogsString = String.join("|", exerciseLogs);

        // L1: Uniamo la sessione con il classico punto e virgola
        return String.join(CSV_DELIMITER,
                convertNullToEmptyString(sessionLog.getUserId()),
                convertNullToEmptyString(sessionLog.getNotes()),
                sessionLog.getStatus() != null ? sessionLog.getStatus().name() : "",
                sessionLog.getDate() != null ? sessionLog.getDate().toString() : "",
                convertNullToEmptyString(sessionLog.getPlanId()),
                String.valueOf(sessionLog.getWorkoutSessionDay()),
                exerciseLogsString
        );
    }

    private String exerciseLogToCsvRow(ExerciseLog exerciseLog) {
        // L4 & L5: Uniamo reps e load con i due punti ":", e i set con la virgola ","
        List<String> sets = exerciseLog.getSets().stream().map(set -> set.reps() + ":" + set.load()).toList();
        String setsString = String.join(",", sets);

        // L3: Uniamo i campi interni dell'esercizio con la tilde "~"
        return String.join("~",
                convertNullToEmptyString(exerciseLog.getName()),
                convertNullToEmptyString(exerciseLog.getExerciseId()),
                setsString,
                String.valueOf(exerciseLog.getRpe()),
                convertNullToEmptyString(exerciseLog.getNotes())
        );
    }

    private SessionLog sessionLogFromCsvRow(String[] parts) throws DaoException {
        // Parsing dei campi standard
        String userId = convertEmptyStringToNull(parts[0]);
        String notes = convertEmptyStringToNull(parts[1]);
        SessionLog.SessionStatus status = (parts[2] != null && !parts[2].isEmpty()) ? SessionLog.SessionStatus.valueOf(parts[2]) : null;
        LocalDateTime date = (parts[3] != null && !parts[3].isEmpty()) ? LocalDateTime.parse(parts[3]) : null;
        String planId = convertEmptyStringToNull(parts[4]);
        int day = (parts[5] != null && !parts[5].isEmpty()) ? Integer.parseInt(parts[5]) : 0;

        // Attenzione: Il tuo costruttore accetta solo 6 parametri, non la lista di esercizi!
        SessionLog sessionLog = new SessionLog(userId, notes, status, date, planId, day);

        // Splittiamo la colonna 6 in base al Pipe "|"
        String exerciseLogsString = convertEmptyStringToNull(parts[6]);
        if (exerciseLogsString != null && !exerciseLogsString.isBlank()) {
            // Essendo il pipe un carattere speciale nelle Regex, va "escapato" con \\
            String[] exerciseLogArray = exerciseLogsString.split("\\|");
            for (String exLogStr : exerciseLogArray) {
                // Aggiungiamo ogni esercizio alla sessione tramite il metodo add
                sessionLog.addExerciseLog(exerciseLogFromString(exLogStr));
            }
        }

        return sessionLog;
    }

    private ExerciseLog exerciseLogFromString(String exLogStr) throws DaoException {
        // Splittiamo la stringa dell'esercizio in base alla tilde "~"
        // Passiamo -1 a split per non perdere le note vuote alla fine della stringa
        String[] parts = exLogStr.split("~", -1);

        String name = convertEmptyStringToNull(parts[0]);
        String exId = convertEmptyStringToNull(parts[1]);
        List<ExerciseLog.ExerciseSet> sets = parseExerciseSets(convertEmptyStringToNull(parts[2]));
        int rpe = (parts[3] != null && !parts[3].isEmpty()) ? Integer.parseInt(parts[3]) : 0;
        String notes = convertEmptyStringToNull(parts[4]);

        return new ExerciseLog(name, exId, sets, rpe, notes);
    }

    private List<ExerciseLog.ExerciseSet> parseExerciseSets(String exerciseSetsString) throws DaoException {
        List<ExerciseLog.ExerciseSet> setsList = new ArrayList<>();
        if (exerciseSetsString == null || exerciseSetsString.isBlank()) return setsList;

        String[] setsArray = exerciseSetsString.split(",");
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
