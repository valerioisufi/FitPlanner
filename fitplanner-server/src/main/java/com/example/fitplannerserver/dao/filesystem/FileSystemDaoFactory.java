package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.dao.DataInitializer;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileSystemDaoFactory extends DaoFactory {

    private static final Logger LOGGER = Logger.getLogger(FileSystemDaoFactory.class.getName());
    private static final String BASE_DIR = "filesystem_db";

    private final FileSystemAccountDao accountDao;
    private final FileSystemProfileDao profileDao;
    private final FileSystemSessionLogDao sessionLogDao;
    private final FileSystemExerciseLibraryDao exerciseLibraryDao;
    private final FileSystemWorkoutPlanDao workoutPlanDao;
    private final FileSystemCoachingDao coachingDao;

    public FileSystemDaoFactory() {
        Path accountsPath = Path.of(BASE_DIR, "accounts.csv");
        Path profilesPath = Path.of(BASE_DIR, "profiles.csv");
        Path sessionLogsPath = Path.of(BASE_DIR, "session_logs.csv");
        Path exerciseLibraryPath = Path.of(BASE_DIR, "exercise_library.csv");
        Path workoutPlansPath = Path.of(BASE_DIR, "workout_plans.csv");
        Path coachingPath = Path.of(BASE_DIR, "coaching.csv");

        accountDao = new FileSystemAccountDao(accountsPath);
        profileDao = new FileSystemProfileDao(profilesPath);
        sessionLogDao = new FileSystemSessionLogDao(sessionLogsPath);
        exerciseLibraryDao = new FileSystemExerciseLibraryDao(exerciseLibraryPath);
        workoutPlanDao = new FileSystemWorkoutPlanDao(workoutPlansPath);
        coachingDao = new FileSystemCoachingDao(coachingPath);

        defaultData();

    }

    private void defaultData(){
        try {
            DataInitializer initializer = new DataInitializer(
                    this.accountDao,
                    this.profileDao,
                    this.coachingDao,
                    this.exerciseLibraryDao
            );
            initializer.initialize();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Impossibile generare gli utenti di default sul File System", e);

        }
    }


    @Override
    public FileSystemAccountDao getAccountDao() {
        return this.accountDao;
    }

    @Override
    public FileSystemProfileDao getProfileDao() {
        return this.profileDao;
    }

    @Override
    public FileSystemSessionLogDao getSessionLogDao() {
        return this.sessionLogDao;
    }

    @Override
    public FileSystemExerciseLibraryDao getExerciseLibraryDao() {
        return this.exerciseLibraryDao;
    }

    @Override
    public FileSystemWorkoutPlanDao getWorkoutPlanDao() {
        return this.workoutPlanDao;
    }

    @Override
    public FileSystemCoachingDao getCoachingDao() {
        return this.coachingDao;
    }


}
