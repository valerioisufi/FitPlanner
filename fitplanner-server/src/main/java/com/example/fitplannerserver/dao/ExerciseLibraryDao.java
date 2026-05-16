package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.ExerciseDescription;

import java.util.List;
import java.util.Optional;

public interface ExerciseLibraryDao {
    // Adds a new exercise to the database, linked to a specific trainer
    void saveExercise(ExerciseDescription exercise) throws DaoException;

    // Removes an exercise from the database
    void deleteExercise(String exerciseId) throws DaoException;

    // Retrieves a single exercise by its UUID
    Optional<ExerciseDescription> findById(String exerciseId) throws DaoException;

    // Retrieves all exercises belonging to a specific trainer
    List<ExerciseDescription> findAllByTrainerId(String trainerId) throws DaoException;

    // Retrieves a list of exercises matching the provided UUIDs.
    List<ExerciseDescription> findByIds(List<String> exerciseIds) throws DaoException;
}