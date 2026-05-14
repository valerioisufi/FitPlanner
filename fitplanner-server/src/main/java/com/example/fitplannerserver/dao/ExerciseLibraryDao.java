package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.model.plan.ExerciseDescription;

import java.util.List;

public interface ExerciseLibraryDao {
    // Adds a new exercise to the database, linked to a specific trainer
    void saveExercise(ExerciseDescription exercise, String trainerUuid);

    // Updates the details of an existing exercise
    void updateExercise(ExerciseDescription exercise);

    // Removes an exercise from the database
    void deleteExercise(String exerciseUuid);

    // Retrieves a single exercise by its UUID
    ExerciseDescription findById(String exerciseUuid);

    // Retrieves all exercises belonging to a specific trainer
    List<ExerciseDescription> findAllByTrainerId(String trainerUuid);

    // Retrieves a list of exercises matching the provided UUIDs.
    List<ExerciseDescription> findByIds(List<String> exerciseUuids);
}