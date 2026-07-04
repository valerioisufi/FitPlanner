package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.ExerciseLibraryDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.ExerciseDescription;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.example.fitplannerserver.dao.filesystem.CsvUtils.*;

public class FileSystemExerciseLibraryDao implements ExerciseLibraryDao {
    private static final String CSV_HEADER= "trainer_id,exercise_id,name,execution,muscle_groups";
    private static final int EXPECTED_COLUMNS = 5;
    private static final String NULL_EXERCISE_ID_MSG = "exercise id cannot be null";
    private final Path path ;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public FileSystemExerciseLibraryDao (Path path){
        this.path= Objects.requireNonNull(path, "path cannot be null");
        initializeFile(this.path, CSV_HEADER);
    }

    @Override
    public void saveExercise(ExerciseDescription exercise) throws DaoException {
        Objects.requireNonNull(exercise, "exercise cannot be null");
        Objects.requireNonNull(exercise.getExerciseId(), NULL_EXERCISE_ID_MSG);

        lock.writeLock().lock();
        try{
            CsvUtils.update(path, EXPECTED_COLUMNS, parts -> parts[1].equals(exercise.getExerciseId()), toCsvRow(exercise));
        }catch (IOException e){
            throw new DaoException("Errore durante il salvataggio dell'esercizio", e);
        }finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteExercise(String exerciseId) throws DaoException {
        Objects.requireNonNull(exerciseId, NULL_EXERCISE_ID_MSG);

        lock.writeLock().lock();
        try{
            CsvUtils.delete(path, EXPECTED_COLUMNS, parts -> parts[1].equals(exerciseId));
        }catch (IOException e){
            throw new DaoException("Errore durante l'eliminazione dell'esercizio", e);
        }finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<ExerciseDescription> findById(String exerciseId) throws DaoException {
        Objects.requireNonNull(exerciseId, NULL_EXERCISE_ID_MSG);
        lock.readLock().lock();
        try {
            CsvResultSet rs = search(path, EXPECTED_COLUMNS, parts -> parts[1].equals(exerciseId), 1);
            if(rs.next()){
                return Optional.of(fromCsvRS(rs));
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new DaoException("Errore durante la ricerca dell'esercizio", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ExerciseDescription> findAllByTrainerId(String trainerId) throws DaoException {
        Objects.requireNonNull(trainerId, "trainer id cannot be null");
        lock.readLock().lock();
        try{
            List<ExerciseDescription> exercise = new ArrayList<>();
            CsvResultSet rs = search(path, EXPECTED_COLUMNS, parts -> parts[0].equals(trainerId), -1);

            while(rs.next()){
                exercise.add(fromCsvRS(rs));
            }
            return exercise;

        } catch (IOException e) {
            throw new DaoException("Errore durante la ricerca degli esercizi del trainer", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    //HELPER
    private String toCsvRow(ExerciseDescription exerciseDescription) {
        List<String> muscles = exerciseDescription.getMuscleGroups() != null ? exerciseDescription.getMuscleGroups() : List.of();

        return new CsvRowBuilder()
                .add(exerciseDescription.getTrainerId())
                .add(exerciseDescription.getExerciseId())
                .add(exerciseDescription.getName())
                .add(exerciseDescription.getExecution())
                .add(String.join(";", muscles))
                .build();
    }

    private ExerciseDescription fromCsvRS(CsvResultSet rs) {

        String muscleGroupsString = rs.getString(4);
        List<String> muscleGroups = (muscleGroupsString == null) ? List.of() : List.of(muscleGroupsString.split(";"));

        return new ExerciseDescription(
                rs.getString(0),
                rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                muscleGroups
        );

    }
}
