package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.DbConnection;
import com.example.fitplannerserver.dao.ExerciseLibraryDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.ExerciseDescription;

import java.sql.*;
import java.util.Objects;

public class DatabaseExerciseLibraryDao implements ExerciseLibraryDao {

    public DatabaseExerciseLibraryDao(){
        createTableIfNotExist();
    }

    private void createTableIfNotExist(){
        String sql= """
                CREATE TABLE IF NOT EXISTS exercise_library(
                exercise_id VARCHAR(36) NOT NULL,
                trainer_id VARCHAR(36) NOT NULL,
                name VARCHAR(255) NOT NULL,
                execution TEXT NOT NULL,
                muscle_groups VARCHAR(255) NOT NULL,
                PRIMARY KEY (exercise_id, trainer_id),
                FOREIGN KEY (trainer_id) REFERENCES accounts(user_id) ON DELETE CASCADE);
                """;

        Connection conn= null;
        try{
            conn = DbConnection.getInstance().getConnection();
            try (Statement stm= conn.createStatement()) {
                stm.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException("Errore SQL: Impossibile creare la tabella 'exercise_library'. " +
                        "Verifica la query o i permessi utente su MySQL.", e);
            }
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException("Errore critico: impossibile inizializzare la tabella 'exercise_library'. " +
                    "Il database è irraggiungibile o i permessi sono errati.", e);
        } finally {
            if (conn != null){
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
    }

    @Override
    public void saveExercise(ExerciseDescription exercise) throws DaoException {
        Objects.requireNonNull(exercise, "exercise cannot be null");
        Objects.requireNonNull(exercise.getExerciseId(), "exercise id cannot be null");
        Objects.requireNonNull(exercise.getTrainerId(), "trainer id cannot be null");

        String sql= """
                UPDATE exercise_library SET name=?, execution=?, muscle_groups=? WHERE (trainer_id=? AND exercise_id=?)
                """;
        Connection conn = null;

        try{
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)){
                stm.setString(1, exercise.getName());
                stm.setString(2, exercise.getExecution());
                stm.setString(3, String.join(",", exercise.getMuscleGroups()));
                stm.setString(4, exercise.getTrainerId());
                stm.setString(5, exercise.getExerciseId());
                stm.executeUpdate();
            }
        }catch (SQLException | InterruptedException e){
            throw new DaoException("Errore critico durante l'aggiornamento dell'esercizio nel database.", e);
        }finally {
            if (conn != null){
                DbConnection.getInstance().releaseConnection(conn);
            }
        }
    }

    @Override
    public void deleteExercise(String exerciseId) throws DaoException {
        Objects.requireNonNull(exerciseId, "exerciseId cannot be null");

        String sql= """
                DELETE FROM exercise_library WHERE exercise_id=?;
                """;
        Connection conn = null;

        try{
            conn = DbConnection.getInstance().getConnection();
            try (PreparedStatement stm = conn.prepareStatement(sql)){
                stm.setString(1, exerciseId);
                stm.executeUpdate();
            }
        }catch (SQLException | InterruptedException e){
            throw new DaoException("Errore critico durante la cancellazione dell'esercizio nel database.", e);
        }finally {
                DbConnection.getInstance().releaseConnection(conn);
        }
    }

}
