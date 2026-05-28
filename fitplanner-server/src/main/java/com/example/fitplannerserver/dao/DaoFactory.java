package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.config.ServerConfigurationManager;
import com.example.fitplannerserver.dao.database.DatabaseDaoFactory;
import com.example.fitplannerserver.dao.filesystem.FileSystemDaoFactory;
import com.example.fitplannerserver.dao.inmemory.InMemoryDaoFactory;

public abstract class DaoFactory {
    private static class Wrapper {
        static final DaoFactory INSTANCE = createInstance();

        private static DaoFactory createInstance() {
            PersistencyLayer persistencyLayerType = ServerConfigurationManager.getInstance().getPersistencyLayer();

            return switch (persistencyLayerType) {
                case FILESYSTEM -> new FileSystemDaoFactory();
                case DATABASE -> new DatabaseDaoFactory();
                default -> new InMemoryDaoFactory();
            };

        }
    }

    public static DaoFactory getInstance() {
        return Wrapper.INSTANCE;
    }

    public abstract AccountDao getAccountDao();

    public abstract ProfileDao getProfileDao();

    public abstract SessionLogDao getSessionLogDao();

    public abstract ExerciseLibraryDao getExerciseLibraryDao();

    public abstract WorkoutPlanDao getWorkoutPlanDao();

    public abstract CoachingDao getCoachingDao();



}
