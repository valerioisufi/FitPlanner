package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.*;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.model.User;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.File;
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
        File accountsPath = new File(BASE_DIR, "accounts.csv");
        File profilesPath = new File(BASE_DIR, "profiles.csv");
        File sessionLogsPath = new File(BASE_DIR, "session_logs.csv");
        File exerciseLibraryPath = new File(BASE_DIR, "exercise_library.csv");
        File workoutPlansPath = new File(BASE_DIR, "workout_plans.csv");
        File coachingPath = new File(BASE_DIR, "coaching.csv");

        accountDao = new FileSystemAccountDao(accountsPath);
        profileDao = new FileSystemProfileDao(profilesPath);
        sessionLogDao = new FileSystemSessionLogDao();
        exerciseLibraryDao = new FileSystemExerciseLibraryDao();
        workoutPlanDao = new FileSystemWorkoutPlanDao();
        coachingDao = new FileSystemCoachingDao();

        defaultData();
    }

    private void defaultData(){
        try {
            if(this.accountDao.findByEmail("trainer@fitplanner.com").isPresent()){
                return;
            }
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String defaultPasswordHash = encoder.encode("password");

            //trainer default
            String trainerId= UuidCreator.getTimeOrderedEpoch().toString();
            Account trainerAccount = new Account(
                    trainerId,
                    "trainer@fitplanner.com",
                    defaultPasswordHash,
                    null,
                    Account.Role.TRAINER);
            this.accountDao.create(trainerAccount);

            User trainerProfile = new User(
                    trainerId,
                    "super",
                    "Trainer",
                    "trainer@fitplanner.com",
                    "1234567890",
                    null
            );
            this.profileDao.save(trainerProfile);

            //athlete default
            String athleteId = UuidCreator.getTimeOrderedEpoch().toString();
            Account athleteAccount = new Account(
                    athleteId,
                    "athlete@fitplanner.com",
                    defaultPasswordHash,
                    null,
                    Account.Role.ATHLETE
            );
            this.accountDao.create(athleteAccount);
            User athleteProfile = new User(
                    athleteId,
                    "Kanye",
                    "West",
                    "athlete@fitplanner.com",
                    "0987654321",
                    null
            );
            this.profileDao.save(athleteProfile);
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
