package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.exception.SystemException;
import com.example.fitplannerserver.model.user.Account;
import com.example.fitplannerserver.model.user.AthleteUser;
import com.example.fitplannerserver.model.user.TrainerUser;
import com.example.fitplannerserver.model.plan.ExerciseDescription;
import com.example.fitplannerserver.util.InvitationCodeGenerator;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

public class DataInitializer {

    private static final String GAMBE = "Gambe";
    private static final String DEFAULT_TRAINER_EMAIL = "trainer@fitplanner.com";

    private final AccountDao accountDao;
    private final ProfileDao profileDao;
    private final ExerciseLibraryDao exerciseLibraryDao;

    public DataInitializer(AccountDao accountDao, ProfileDao profileDao,
                           ExerciseLibraryDao exerciseLibraryDao) {
        this.accountDao = accountDao;
        this.profileDao = profileDao;
        this.exerciseLibraryDao = exerciseLibraryDao;
    }

    public void initialize() {
        try {
            if (accountDao.findByEmail(DEFAULT_TRAINER_EMAIL).isPresent()) {
                return;
            }

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String defaultPasswordHash = encoder.encode("password");

            // Default Trainer
            String trainerId = UuidCreator.getTimeOrderedEpoch().toString();
            Account trainerAccount = new Account(
                    trainerId,
                    DEFAULT_TRAINER_EMAIL, defaultPasswordHash, null,
                    Account.Role.TRAINER
            );
            this.accountDao.create(trainerAccount);

            TrainerUser trainerProfile = new TrainerUser(
                    trainerId,
                    "Super",
                    "Trainer",
                    DEFAULT_TRAINER_EMAIL,
                    "1234567890",
                    InvitationCodeGenerator.generateCode()
            );
            this.profileDao.save(trainerProfile);

            // Default Athlete
            String athleteId = UuidCreator.getTimeOrderedEpoch().toString();
            createAthlete(athleteId, "John", "Doe", "athlete@fitplanner.com", defaultPasswordHash, trainerProfile);

            // Altri atleti
            String[] otherAthletes = {
                    "Mario Rossi,mario@fitplanner.com",
                    "Luigi Bianchi,luigi@fitplanner.com",
                    "Giulia Verdi,giulia@fitplanner.com",
                    "Anna Neri,anna@fitplanner.com"
            };

            for (String athleteInfo : otherAthletes) {
                String[] parts = athleteInfo.split(",");
                String[] nameParts = parts[0].split(" ");
                String id = UuidCreator.getTimeOrderedEpoch().toString();
                createAthlete(id, nameParts[0], nameParts[1], parts[1], defaultPasswordHash, trainerProfile);
            }

            // Crea esercizi per il trainer
            createExercise(trainerId, "Squat", "Esecuzione dello squat con bilanciere", List.of(GAMBE, "Glutei"));
            createExercise(trainerId, "Panca Piana", "Distensioni su panca piana con bilanciere", List.of("Petto", "Tricipiti", "Spalle"));
            createExercise(trainerId, "Trazioni", "Trazioni alla sbarra presa prona", List.of("Schiena", "Bicipiti"));
            createExercise(trainerId, "Stacco da terra", "Stacco da terra regolare", List.of("Schiena", GAMBE, "Glutei"));
            createExercise(trainerId, "Military Press", "Spinte in alto con bilanciere in piedi", List.of("Spalle", "Tricipiti"));
            createExercise(trainerId, "Leg Press", "Pressa a 45 gradi", List.of(GAMBE));

        } catch (DaoException e) {
            throw new SystemException("Errore durante l'inizializzazione dei dati", e);
        }
    }

    private void createAthlete(String athleteId, String firstName, String lastName, String email, String passwordHash, TrainerUser trainer) throws DaoException {
        Account athleteAccount = new Account(
                athleteId,
                email,
                passwordHash,
                null,
                Account.Role.ATHLETE
        );
        this.accountDao.create(athleteAccount);

        AthleteUser athleteProfile = new AthleteUser(athleteId);
        athleteProfile.setUserProfileInfo(firstName, lastName, email, "0000000000");
        athleteProfile.linkTo(trainer);

        this.profileDao.save(athleteProfile);
    }

    private void createExercise(String trainerId, String name, String execution, List<String> muscleGroups) throws DaoException {
        String exerciseId = UuidCreator.getTimeOrderedEpoch().toString();
        ExerciseDescription exercise = new ExerciseDescription(trainerId, exerciseId, name, execution, muscleGroups);
        this.exerciseLibraryDao.saveExercise(exercise);
    }
}
