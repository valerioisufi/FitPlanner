package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.DbConnection;
import com.example.fitplannerserver.dao.ExerciseLibraryDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.ExerciseDescription;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DatabaseExerciseLibraryDao implements ExerciseLibraryDao {

    @Override
    public void saveExercise(ExerciseDescription exercise) throws DaoException {
        Objects.requireNonNull(exercise, "exercise cannot be null");
        Objects.requireNonNull(exercise.getExerciseId(), "exercise id cannot be null");
        Objects.requireNonNull(exercise.getTrainerId(), "trainer id cannot be null");

        String sql = """
                UPDATE exercise_library SET name=?, execution=?, muscle_groups=? WHERE (trainer_id=? AND exercise_id=?)
                """;
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, exercise.getName());
                stm.setString(2, exercise.getExecution());
                stm.setString(3, String.join(",", exercise.getMuscleGroups()));
                stm.setString(4, exercise.getTrainerId());
                stm.setString(5, exercise.getExerciseId());
                stm.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante l'aggiornamento dell'esercizio nel database.", e);
        } finally {
            if (conn != null) {
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
    }

    @Override
    public void deleteExercise(String exerciseId) throws DaoException {
        Objects.requireNonNull(exerciseId, "exerciseId cannot be null");

        String sql = """
                DELETE FROM exercise_library WHERE exercise_id=?;
                """;
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, exerciseId);
                stm.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la cancellazione dell'esercizio nel database.", e);
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public Optional<ExerciseDescription> findById(String exerciseId) throws DaoException {
        Objects.requireNonNull(exerciseId, "exerciseId cannot be null");

        String sql = """
                SELECT * FROM exercise_library WHERE exercise_id=?;
                """;
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, exerciseId);
                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next()) {
                        ExerciseDescription exercise = new ExerciseDescription(
                                rs.getString("trainer_id"),
                                rs.getString("exercise_id"),
                                rs.getString("name"),
                                rs.getString("execution"),
                                List.of(rs.getString("muscle_groups").split(",")));
                        return Optional.of(exercise);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la ricerca dell'esercizio nel database.", e);
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public List<ExerciseDescription> findAllByTrainerId(String trainerId) throws DaoException {
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

        String sql = """
                SELECT * FROM exercise_library WHERE trainer_id=?;
                """;
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, trainerId);
                try (ResultSet rs = stm.executeQuery()) {
                    List<ExerciseDescription> exercises = new java.util.ArrayList<>();
                    while (rs.next()) {
                        ExerciseDescription exercise = new ExerciseDescription(
                                rs.getString("exercise_id"),
                                rs.getString("trainer_id"),
                                rs.getString("name"),
                                rs.getString("execution"),
                                List.of(rs.getString("muscle_group").split(",")));
                        exercises.add(exercise);
                    }
                    return exercises;
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la ricerca dell'esercizio nel database.", e);
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
    }

    @Override
    public List<ExerciseDescription> findByIds(List<String> exerciseIds) throws DaoException {
        Objects.requireNonNull(exerciseIds, "exerciseIds cannot be null");

        List<ExerciseDescription> exercises = new java.util.ArrayList<>();
        String sql = """
                SELECT * FROM exercise_library WHERE exercise_id IN (?);
                """;
        Connection conn = null;

        try {
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)) {
                stm.setString(1, String.join(",", exerciseIds));
                try (ResultSet rs = stm.executeQuery()) {
                    while (rs.next()) {
                        ExerciseDescription exercise = new ExerciseDescription(
                                rs.getString("exercise_id"),
                                rs.getString("trainer_id"),
                                rs.getString("name"),
                                rs.getString("execution"),
                                List.of(rs.getString("muscle_group").split(",")));
                        exercises.add(exercise);
                    }
                    return exercises;
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Errore critico durante la ricerca dell'esercizio nel database.", e);
        } finally {
            DbConnection.getInstance().releaseConnection(conn);
        }
    }


}
