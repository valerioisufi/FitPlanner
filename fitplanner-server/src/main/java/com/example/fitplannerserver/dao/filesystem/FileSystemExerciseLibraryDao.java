package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.ExerciseLibraryDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.ExerciseDescription;

import java.util.List;
import java.util.Optional;

public class FileSystemExerciseLibraryDao implements ExerciseLibraryDao {
    @Override
    public void saveExercise(ExerciseDescription exercise) throws DaoException {

    }

    @Override
    public void deleteExercise(String exerciseId) throws DaoException {

    }

    @Override
    public Optional<ExerciseDescription> findById(String exerciseId) throws DaoException {
        return Optional.empty();
    }

    @Override
    public List<ExerciseDescription> findAllByTrainerId(String trainerId) throws DaoException {
        return List.of();
    }

    @Override
    public List<ExerciseDescription> findByIds(List<String> exerciseIds) throws DaoException {
        return List.of();
    }
}
