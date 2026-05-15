package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.ExerciseLibraryDao;
import com.example.fitplannerserver.model.log.SessionLog;
import com.example.fitplannerserver.model.plan.ExerciseDescription;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryExerciseLibraryDao implements ExerciseLibraryDao {

    private static class Wrapper {
        public static final InMemoryExerciseLibraryDao INSTANCE = new InMemoryExerciseLibraryDao();
    }

    private InMemoryExerciseLibraryDao(){}

    public static InMemoryExerciseLibraryDao getInstance() {
        return Wrapper.INSTANCE;
    }

    // Map Key: exerciseId
    private final Map<String, List<SessionLog>> exerciseDescriptions = new ConcurrentHashMap<>();

    @Override
    public void saveExercise(ExerciseDescription exercise, String trainerUuid) {

    }

    @Override
    public void updateExercise(ExerciseDescription exercise) {

    }

    @Override
    public void deleteExercise(String exerciseUuid) {

    }

    @Override
    public ExerciseDescription findById(String exerciseUuid) {
        return null;
    }

    @Override
    public List<ExerciseDescription> findAllByTrainerId(String trainerUuid) {
        return List.of();
    }

    @Override
    public List<ExerciseDescription> findByIds(List<String> exerciseUuids) {
        return List.of();
    }

}
