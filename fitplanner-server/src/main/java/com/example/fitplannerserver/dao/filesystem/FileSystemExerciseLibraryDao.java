package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.ExerciseLibraryDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.ExerciseDescription;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.example.fitplannerserver.dao.filesystem.CsvUtils.*;

public class FileSystemExerciseLibraryDao implements ExerciseLibraryDao {
    private static final String CSV_HEADER= "trainer_id;exercise_id;name;execution;muscle_groups";
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
            return search(path, EXPECTED_COLUMNS, parts -> parts[1].equals(exerciseId), 1)
                    .stream().findFirst().map(this::fromCsvRow);
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
            return search(path, EXPECTED_COLUMNS, parts -> parts[0].equals(trainerId), -1)
                    .stream().map(this::fromCsvRow).toList();
        } catch (IOException e) {
            throw new DaoException("Errore durante la ricerca degli esercizi del trainer", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<ExerciseDescription> findByIds(List<String> exerciseIds) throws DaoException {
        if(exerciseIds == null || exerciseIds.isEmpty()){
            return List.of();
        }
        lock.readLock().lock();
        try{
            List<ExerciseDescription> foundExercises = CsvUtils.search(path, EXPECTED_COLUMNS, parts -> exerciseIds.contains(parts[1]), -1)
                    .stream()
                    .map(this::fromCsvRow)
                    .toList();

            if (foundExercises.size() < exerciseIds.size()) {
                List<String> foundIds = foundExercises.stream()
                        .map(ExerciseDescription::getExerciseId)
                        .toList();
                List<String> missingIds = new java.util.ArrayList<>(exerciseIds);
                missingIds.removeAll(foundIds);
                throw new DaoException("I seguenti esercizi non sono stati trovati nel database: " + missingIds);
            }

            return foundExercises;
        }catch (IOException e){
            throw new DaoException("Errore durante la ricerca degli esercizi", e);
        }finally {
            lock.readLock().unlock();
        }
    }

    //HELPER
    private String toCsvRow(ExerciseDescription exerciseDescription) {
        List<String> muscles = exerciseDescription.getMuscleGroups() != null ? exerciseDescription.getMuscleGroups() : List.of();
        return String.join(CSV_DELIMITER,
                convertNullToEmptyString(exerciseDescription.getTrainerId()),
                convertNullToEmptyString(exerciseDescription.getExerciseId()),
                convertNullToEmptyString(exerciseDescription.getName()),
                convertNullToEmptyString(exerciseDescription.getExecution()),
                convertNullToEmptyString(String.join(",", muscles))
        );
    }

    private ExerciseDescription fromCsvRow(String[] parts) {

        String muscleGroupsString = convertEmptyStringToNull(parts[4]);
        List<String> muscleGroups = (muscleGroupsString == null) ? List.of() : List.of(muscleGroupsString.split(","));

        return new ExerciseDescription(
                convertEmptyStringToNull(parts[0]),
                convertEmptyStringToNull(parts[1]),
                convertEmptyStringToNull(parts[2]),
                convertEmptyStringToNull(parts[3]),
                muscleGroups
        );

    }
}
