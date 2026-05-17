package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.DaoFactory;

public class InMemoryDaoFactory extends DaoFactory {

    private final InMemoryAccountDao accountDao;
    private final InMemoryProfileDao profileDao;
    private final InMemorySessionLogDao sessionLogDao;
    private final InMemoryExerciseLibraryDao exerciseLibraryDao;
    private final InMemoryWorkoutPlanDao workoutPlanDao;
    private final InMemoryCoachingDao coachingDao;

    public InMemoryDaoFactory() {
        this.accountDao = new InMemoryAccountDao();
        this.profileDao = new InMemoryProfileDao();
        this.sessionLogDao = new InMemorySessionLogDao();
        this.exerciseLibraryDao = new InMemoryExerciseLibraryDao();
        this.workoutPlanDao = new InMemoryWorkoutPlanDao();
        this.coachingDao = new InMemoryCoachingDao();

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
    public InMemoryCoachingDao getCoachingDao() {
        return this.coachingDao;
    }

}
