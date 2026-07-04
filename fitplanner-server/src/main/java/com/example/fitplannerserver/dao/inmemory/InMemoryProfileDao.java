package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.user.AthleteUser;
import com.example.fitplannerserver.model.user.TrainerUser;
import com.example.fitplannerserver.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProfileDao implements ProfileDao {

    // Map Key: userId
    private final Map<String, User> profiles = new ConcurrentHashMap<>();
    private static final String USER_ID_CANNOT_BE_NULL = "userId cannot be null";

    @Override
    public Optional<User> findById(String userId) {
        Objects.requireNonNull(userId, USER_ID_CANNOT_BE_NULL);
        return Optional.ofNullable(profiles.get(userId)).map(User::copy);
    }

    @Override
    public void save(User user) {
        Objects.requireNonNull(user, "user cannot be null");
        Objects.requireNonNull(user.getId(), USER_ID_CANNOT_BE_NULL);
        profiles.put(user.getId(), user.copy());
    }

    @Override
    public Optional<TrainerUser> findByInvitationCode(String invitationCode) {
        if (invitationCode == null || invitationCode.isBlank()) {
            return Optional.empty();
        }
        for (User user : profiles.values()) {
            if (user instanceof TrainerUser trainer && invitationCode.equals(trainer.getInvitationCode())) {
                return Optional.of((TrainerUser) trainer.copy());
            }
        }
        return Optional.empty();
    }

    @Override
    public List<AthleteUser> findAthletesByTrainerId(String trainerId) {
        Objects.requireNonNull(trainerId, "trainerId cannot be null");
        List<AthleteUser> athletes = new ArrayList<>();
        for (User user : profiles.values()) {
            if (user instanceof AthleteUser athlete && trainerId.equals(athlete.getTrainerId())) {
                athletes.add((AthleteUser) athlete.copy());
            }
        }
        return athletes;
    }

    @Override
    public Optional<AthleteUser> findAthleteById(String athleteId) throws DaoException {
        return findById(athleteId).filter(AthleteUser.class::isInstance).map(AthleteUser.class::cast);
    }

    @Override
    public Optional<TrainerUser> findTrainerById(String trainerId) throws DaoException {
        return findById(trainerId).filter(TrainerUser.class::isInstance).map(TrainerUser.class::cast);
    }

}
