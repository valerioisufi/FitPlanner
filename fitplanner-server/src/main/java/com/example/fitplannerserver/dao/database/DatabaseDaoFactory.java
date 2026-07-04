package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.DaoFactory;

public class DatabaseDaoFactory extends DaoFactory {

    private final DatabaseAccountDao accountDao;
    private final DatabaseProfileDao profileDao;
    private final DatabaseSessionLogDao sessionLogDao;
    private final DatabaseExerciseLibraryDao exerciseLibraryDao;
    private final DatabaseWorkoutPlanDao workoutPlanDao;

    public DatabaseDaoFactory(){
        this.accountDao = new DatabaseAccountDao();
        this.profileDao = new DatabaseProfileDao();
        this.sessionLogDao = new DatabaseSessionLogDao();
        this.exerciseLibraryDao = new DatabaseExerciseLibraryDao();
        this.workoutPlanDao = new DatabaseWorkoutPlanDao();
    }

    @Override
    public DatabaseAccountDao getAccountDao() {
        return this.accountDao;
    }

    @Override
    public DatabaseProfileDao getProfileDao() {
        return this.profileDao;
    }

    @Override
    public DatabaseSessionLogDao getSessionLogDao() {
            return this.sessionLogDao;
    }

    @Override
    public DatabaseExerciseLibraryDao getExerciseLibraryDao() {
        return this.exerciseLibraryDao;
    }

    @Override
    public DatabaseWorkoutPlanDao getWorkoutPlanDao() {
        return this.workoutPlanDao;
    }

}
