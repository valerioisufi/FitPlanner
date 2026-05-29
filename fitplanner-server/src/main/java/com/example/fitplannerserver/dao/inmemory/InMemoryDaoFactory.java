package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.model.User;
import com.example.fitplannerserver.util.InvitationCodeGenerator;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

        // --- Credenziali di Default ---
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String defaultPasswordHash = encoder.encode("password");

        // Default Trainer
        String trainerId = UuidCreator.getTimeOrderedEpoch().toString();
        Account trainerAccount = new Account(
                trainerId,
                "trainer@fitplanner.com", defaultPasswordHash, null,
                Account.Role.TRAINER
        );
        this.accountDao.create(trainerAccount);

        User trainerProfile = new User(
                trainerId,
                "Super",
                "Trainer",
                "trainer@fitplanner.com",
                "1234567890",
                null
        );
        trainerProfile.setInvitationCode(InvitationCodeGenerator.generateCode());
        this.profileDao.save(trainerProfile);

        // Default Athlete
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
                "John",
                "Doe",
                "athlete@fitplanner.com",
                "0987654321",
                null
        );
        this.profileDao.save(athleteProfile);
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
