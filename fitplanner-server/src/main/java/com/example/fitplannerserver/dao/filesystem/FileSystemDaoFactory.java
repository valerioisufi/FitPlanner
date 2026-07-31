package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class FileSystemDaoFactory extends DaoFactory {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemDaoFactory.class);
    private static final String BASE_DIR = "filesystem_db";

    private final FileSystemAccountDao accountDao;
    private final FileSystemProfileDao profileDao;
    private final FileSystemSessionLogDao sessionLogDao;
    private final FileSystemExerciseLibraryDao exerciseLibraryDao;
    private final FileSystemWorkoutPlanDao workoutPlanDao;
    private final FileSystemWorkoutSessionDao workoutSessionDao;

    public FileSystemDaoFactory() {
        Path accountsPath = Path.of(BASE_DIR, "accounts.csv");
        Path profilesPath = Path.of(BASE_DIR, "profiles.csv");
        Path sessionLogsPath = Path.of(BASE_DIR, "session_logs.csv");
        Path exerciseLogsPath = Path.of(BASE_DIR, "exercise_logs.csv");
        Path exerciseLibraryPath = Path.of(BASE_DIR, "exercise_library.csv");
        Path workoutPlansPath = Path.of(BASE_DIR, "workout_plans.csv");
        Path workoutSessionsPath = Path.of(BASE_DIR, "workout_sessions.csv");

        profileDao = new FileSystemProfileDao(profilesPath);
        accountDao = new FileSystemAccountDao(accountsPath, profileDao);
        exerciseLibraryDao = new FileSystemExerciseLibraryDao(exerciseLibraryPath);
        workoutSessionDao = new FileSystemWorkoutSessionDao(workoutSessionsPath);
        workoutPlanDao = new FileSystemWorkoutPlanDao(workoutPlansPath, workoutSessionDao);
        sessionLogDao = new FileSystemSessionLogDao(sessionLogsPath, exerciseLogsPath);

        defaultData();
    }

    private void defaultData() {
        try {
            DataInitializer initializer = new DataInitializer(
                    this.accountDao,
                    this.profileDao,
                    this.exerciseLibraryDao
            );
            initializer.initialize();
        } catch (Exception e) {
            logger.error("Impossibile generare gli utenti di default sul File System", e);
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
    public WorkoutSessionDao getWorkoutSessionDao() {
        return this.workoutSessionDao;
    }

}
