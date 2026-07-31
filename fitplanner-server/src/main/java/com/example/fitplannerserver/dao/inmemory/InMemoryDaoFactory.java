package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.dao.DataInitializer;

public class InMemoryDaoFactory extends DaoFactory {

    private final InMemoryAccountDao accountDao;
    private final InMemoryProfileDao profileDao;
    private final InMemoryExerciseLibraryDao exerciseLibraryDao;
    private final InMemoryWorkoutPlanDao workoutPlanDao;
    private final InMemoryWorkoutSessionDao workoutSessionDao;
    private final InMemorySessionLogDao sessionLogDao;

    public InMemoryDaoFactory() {
        this.profileDao = new InMemoryProfileDao();
        this.accountDao = new InMemoryAccountDao(this.profileDao);
        this.exerciseLibraryDao = new InMemoryExerciseLibraryDao();
        this.workoutSessionDao = new InMemoryWorkoutSessionDao();
        this.workoutPlanDao = new InMemoryWorkoutPlanDao(this.workoutSessionDao);
        this.sessionLogDao = new InMemorySessionLogDao();

        DataInitializer initializer = new DataInitializer(
                this.accountDao,
                this.profileDao,
                this.exerciseLibraryDao
        );
        initializer.initialize();
    }

    @Override
    public InMemoryAccountDao getAccountDao() {
        return this.accountDao;
    }

    @Override
    public InMemoryProfileDao getProfileDao() {
        return this.profileDao;
    }

    @Override
    public InMemorySessionLogDao getSessionLogDao() {
        return this.sessionLogDao;
    }

    @Override
    public InMemoryExerciseLibraryDao getExerciseLibraryDao() {
        return this.exerciseLibraryDao;
    }

    @Override
    public InMemoryWorkoutPlanDao getWorkoutPlanDao() {
        return this.workoutPlanDao;
    }

    @Override
    public InMemoryWorkoutSessionDao getWorkoutSessionDao() {
        return this.workoutSessionDao;
    }

}
