package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.ExerciseLibraryDao;
import com.example.fitplannerserver.model.plan.ExerciseDescription;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryExerciseLibraryDao implements ExerciseLibraryDao {

    // Map Key: exerciseId
    private final Map<String, ExerciseDescription> exerciseIdToExerciseDescription = new ConcurrentHashMap<>();

    // Map Key: trainerId
    private final Map<String, List<String>> trainerIdToExerciseIds = new ConcurrentHashMap<>();

    @Override
    public synchronized void saveExercise(ExerciseDescription exercise) {
        Objects.requireNonNull(exercise, "exercise cannot be null");
        Objects.requireNonNull(exercise.getExerciseId(), "exerciseId cannot be null");
        Objects.requireNonNull(exercise.getTrainerId(), "trainerId cannot be null");

        ExerciseDescription copyOfExercise = new ExerciseDescription(exercise);

        exerciseIdToExerciseDescription.put(copyOfExercise.getExerciseId(), copyOfExercise);

        List<String> trainerExerciseIds = trainerIdToExerciseIds.computeIfAbsent(
                copyOfExercise.getTrainerId(),
                k -> new CopyOnWriteArrayList<>()
        );

        if (!trainerExerciseIds.contains(copyOfExercise.getExerciseId())) {
            trainerExerciseIds.add(copyOfExercise.getExerciseId());
        }

    }

    @Override
    public synchronized void deleteExercise(String exerciseId) {
        Objects.requireNonNull(exerciseId, "exerciseId cannot be null");

        ExerciseDescription exerciseToDelete = exerciseIdToExerciseDescription.get(exerciseId);
        if (exerciseToDelete != null) {
            // Remove the ID from the trainer's list
            List<String> trainerList = trainerIdToExerciseIds.get(exerciseToDelete.getTrainerId());
            if (trainerList != null) {
                trainerList.remove(exerciseId);

                if (trainerList.isEmpty()) {
                    trainerIdToExerciseIds.remove(exerciseToDelete.getTrainerId());
                }
            }

            exerciseIdToExerciseDescription.remove(exerciseId);
        }

    }

    @Override
    public Optional<ExerciseDescription> findById(String exerciseId) {
        Objects.requireNonNull(exerciseId, "exerciseId cannot be null");

        return Optional.ofNullable(exerciseIdToExerciseDescription.get(exerciseId))
                .map(ExerciseDescription::new);
    }

    @Override
    public List<ExerciseDescription> findAllByTrainerId(String trainerId) {
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

        List<String> exerciseIds = trainerIdToExerciseIds.get(trainerId);

        if (exerciseIds == null || exerciseIds.isEmpty()) {
            return List.of();
        }

        return getExerciseDescriptions(exerciseIds);
    }

    @Override
    public List<ExerciseDescription> findByIds(List<String> exerciseIds) {
        Objects.requireNonNull(exerciseIds, "exerciseIds cannot be null");

        return getExerciseDescriptions(exerciseIds);
    }

    private List<ExerciseDescription> getExerciseDescriptions(List<String> exerciseIds) {
        List<ExerciseDescription> result = new ArrayList<>();
        for (String exerciseId : exerciseIds) {
            ExerciseDescription ex = exerciseIdToExerciseDescription.get(exerciseId);
            if (ex != null) {
                result.add(new ExerciseDescription(ex));
            }
        }
        return result;
    }

}
