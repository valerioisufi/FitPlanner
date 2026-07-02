package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.DbConnection;
import com.example.fitplannerserver.dao.SessionLogDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.log.ExerciseLog;
import com.example.fitplannerserver.model.log.SessionLog;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseSessionLogDao implements SessionLogDao {

    private static final String NULL_ATHLETE_ID_MSG = "athleteId cannot be null";

    @Override
    public void saveSessionLog(SessionLog log) throws DaoException {
        Objects.requireNonNull(log, "log cannot be null");
        Objects.requireNonNull(log.getUserId(), NULL_ATHLETE_ID_MSG);

        Connection conn = null;
        try {
            conn = DbConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            long sessionId = insertSession(conn, log);

            insertExercise(conn, log.getExerciseLogs(), sessionId);

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
    public List<SessionLog> findLogsByAthleteIdAndDateRange(String athleteId, long startTimestamp, long endTimestamp) throws DaoException {
        Objects.requireNonNull(athleteId, NULL_ATHLETE_ID_MSG);

        String sql= """
                SELECT SL.session_id, SL.user_id, SL.plan_referenced, SL.status, SL.notes, SL.date, EL.exercise_id, EL.order_index, EL.exercise_set, EL.rpe, EL.name, EL.note
                FROM session_log SL LEFT JOIN exercise_log EL ON SL.session_id=EL.session_id
                WHERE SL.user_id=? AND SL.date BETWEEN ? AND ?
                ORDER BY SL.date DESC, EL.order_index ASC
                """;
        Connection conn = null;
        try{
            conn = DbConnection.getInstance().getConnection();
            try(PreparedStatement stm= conn.prepareStatement(sql)){
                stm.setString(1, athleteId);
                stm.setObject(2, LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimestamp), ZoneOffset.UTC));
                stm.setObject(3, LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimestamp), ZoneOffset.UTC));
                try(ResultSet rs = stm.executeQuery()){
                    return extractLogs(rs);
                }
            }
        }catch (SQLException e){
            throw new DaoException("Errore critico durante la ricerca dei log nel database", e);
        }finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Optional<SessionLog> findMostRecentSessionContainingExercise(String athleteId, String exerciseUuid) throws DaoException {
        Objects.requireNonNull(athleteId, NULL_ATHLETE_ID_MSG);
        Objects.requireNonNull(exerciseUuid, "exerciseUuid cannot be null");

        String sql= """
                SELECT SL.session_id, SL.user_id, SL.plan_referenced, SL.workout_session_day, SL.status, SL.notes, SL.date,\s
                                              EL.exercise_id, EL.order_index, EL.exercise_set, EL.rpe, EL.name, EL.note
                                       FROM session_log SL\s
                                       LEFT JOIN exercise_log EL ON SL.session_id = EL.session_id
                                       WHERE SL.session_id = (
                                           SELECT SL_sub.session_id
                                           FROM session_log SL_sub
                                           JOIN exercise_log EL_sub ON SL_sub.session_id = EL_sub.session_id
                                           WHERE SL_sub.user_id = ? AND EL_sub.exercise_id = ?
                                           ORDER BY SL_sub.date DESC
                                           LIMIT 1
                                       )
                                       ORDER BY EL.order_index ASC
                """;
        Connection conn = null;
        try{
            conn = DbConnection.getInstance().getConnection();
            try(PreparedStatement stm= conn.prepareStatement(sql)) {
                stm.setString(1, athleteId);
                stm.setString(2, exerciseUuid);
                try (ResultSet rs = stm.executeQuery()) {
                    List<SessionLog> sessionLogs = extractLogs(rs);
                    if (!sessionLogs.isEmpty()) {
                        return Optional.of(sessionLogs.getFirst());
                    }
                }
            }
        }catch (SQLException e){
            throw new DaoException("Errore critico durante la ricerca dei log nel database", e);
        }finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    private long insertSession(Connection conn, SessionLog log) throws SQLException, DaoException {
        String sqlSession= """
                INSERT INTO session_log (user_id, plan_referenced, workout_session_day, status, notes, date) VALUES (?,?,?,?,?,?)
                """;

        try(PreparedStatement sessionStm = conn.prepareStatement(sqlSession, Statement.RETURN_GENERATED_KEYS)) {
            sessionStm.setString(1, log.getUserId());
            sessionStm.setString(2, log.getPlanId());
            sessionStm.setInt(3, log.getWorkoutSessionDay());
            sessionStm.setString(4, log.getStatus().name());
            sessionStm.setString(5, log.getNotes());
            sessionStm.setObject(6, log.getDate());

            sessionStm.executeUpdate();
            try (ResultSet rs = sessionStm.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    throw new DaoException("Errore critico: impossibile ottenere il session_id generato");
                }
            }
        }
    }

    private void insertExercise(Connection conn, List<ExerciseLog> exerciseLogs, long sessionId) throws SQLException {
        String sqlExercise= """
                INSERT INTO exercise_log (session_id, exercise_id, order_index, exercise_set, rpe, name, note) VALUES (?,?,?,?,?,?,?)
                """;
        try(PreparedStatement exerciseStm = conn.prepareStatement(sqlExercise)){
            exerciseStm.setLong(1, sessionId);
            for(int i=0; i<exerciseLogs.size(); i++){
                ExerciseLog exerciseLog = exerciseLogs.get(i);
                String serializedSet = exerciseLog.getSets().stream().map(Object::toString).collect(Collectors.joining(";"));
                exerciseStm.setString(2, exerciseLog.getExerciseId());
                exerciseStm.setInt(3, i+1);
                exerciseStm.setString(4, serializedSet);
                exerciseStm.setInt(5, exerciseLog.getRpe());
                exerciseStm.setString(6, exerciseLog.getName());
                exerciseStm.setString(7, exerciseLog.getNotes());
                exerciseStm.addBatch();
            }
            exerciseStm.executeBatch();
        }
    }

    private List<SessionLog> extractLogs(ResultSet rs) throws SQLException {
        Map<Long, SessionLog> map= new LinkedHashMap<>();
        while(rs.next()) {
            long sessionId = rs.getLong("session_id");
            SessionLog currentSession = map.get(sessionId);
            
            if (currentSession == null) {
                currentSession = new SessionLog(
                        rs.getString("user_id"),
                        rs.getString("notes"),
                        SessionLog.SessionStatus.valueOf(rs.getString("status")),
                        rs.getObject("date", LocalDateTime.class),
                        rs.getString("plan_referenced"),
                        rs.getInt("workout_session_day")
                );
                map.put(sessionId, currentSession);
            }
            
            String exerciseId= rs.getString("exercise_id");
            if(exerciseId!=null){
                String rawSet=rs.getString("exercise_set");
                List<ExerciseLog.ExerciseSet> parsedSet= parseSetsFromString(rawSet);
                ExerciseLog exercise= new ExerciseLog(
                        rs.getString("name"),
                        exerciseId,
                        parsedSet,
                        rs.getInt("rpe"),
                        rs.getString("note")
                );
                currentSession.addExerciseLog(exercise);
            }
        }
        return new ArrayList<>(map.values());
    }

    private List<ExerciseLog.ExerciseSet> parseSetsFromString(String rawSets) {
        List<ExerciseLog.ExerciseSet> sets = new ArrayList<>();
        // Se il campo nel DB è vuoto o null, restituiamo una lista vuota sicura
        if (rawSets == null || rawSets.isBlank()) {
            return sets;
        }

        // 1. Separiamo le varie serie usando il punto e virgola
        String[] setChunks = rawSets.split(";");

        for (String chunk : setChunks) {
            // ESEMPIO CHUNK: "ExerciseSet[reps=10, load=50.0]"

            int startIndex = chunk.indexOf('[');
            int endIndex = chunk.indexOf(']');

            if (startIndex == -1 || endIndex == -1) {
                continue; // Salta se la stringa è corrotta
            }

            // 2. Ritagliamo solo l'interno: "reps=10, load=50.0"
            String innerData = chunk.substring(startIndex + 1, endIndex);

            // 3. Dividiamo i due parametri usando la virgola e lo spazio
            String[] pairs = innerData.split(", ");

            int reps = 0;
            double load = 0.0;

            // 4. Leggiamo la coppia chiave-valore
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length < 2) continue; // Sicurezza anti-crash

                String key = keyValue[0].trim();
                String value = keyValue[1].trim();

                if (key.equals("reps")) {
                    reps = Integer.parseInt(value);
                } else if (key.equals("load")) {
                    load = Double.parseDouble(value); // Mappato sul tuo double load!
                }
            }

            // 5. Creiamo il record reale e lo aggiungiamo alla lista
            sets.add(new ExerciseLog.ExerciseSet(reps, load));
        }

        return sets;
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
                e.printStackTrace();
            } finally {
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
    }



}
