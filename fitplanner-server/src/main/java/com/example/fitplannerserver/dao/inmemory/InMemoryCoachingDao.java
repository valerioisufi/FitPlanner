package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.CoachingDao;
import com.example.fitplannerserver.exception.DaoException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryCoachingDao implements CoachingDao {

    private static final String ATHLETE_ID_CANNOT_BE_NULL = "athleteId cannot be null";
    private static final String TRAINER_ID_CANNOT_BE_NULL = "trainerId cannot be null";

    // Map: trainerId -> List of athleteIds
    private final Map<String, List<String>> trainerToAthletes = new ConcurrentHashMap<>();

    // Map: athleteId -> trainerId
    private final Map<String, String> athleteToTrainer = new ConcurrentHashMap<>();

    @Override
    public synchronized void linkAthleteToTrainer(String athleteId, String trainerId) throws DaoException {
        Objects.requireNonNull(athleteId, ATHLETE_ID_CANNOT_BE_NULL);
        Objects.requireNonNull(trainerId, TRAINER_ID_CANNOT_BE_NULL);

        if (athleteToTrainer.containsKey(athleteId)) {
            throw new DaoException("L'atleta è già collegato a un trainer");
        }

        trainerToAthletes.computeIfAbsent(trainerId, k -> new CopyOnWriteArrayList<>()).add(athleteId);

        athleteToTrainer.put(athleteId, trainerId);
    }

    @Override
    public synchronized void unlink(String athleteId, String trainerId) {
        Objects.requireNonNull(athleteId, ATHLETE_ID_CANNOT_BE_NULL);
        Objects.requireNonNull(trainerId, TRAINER_ID_CANNOT_BE_NULL);

        List<String> athletes = trainerToAthletes.get(trainerId);
        if (athletes != null) {
            athletes.remove(athleteId);

            if (athletes.isEmpty()) {
                trainerToAthletes.remove(trainerId);
            }
        }

        athleteToTrainer.remove(athleteId, trainerId);
    }

    @Override
    public boolean isClientOf(String trainerId, String athleteId) {
        Objects.requireNonNull(athleteId, ATHLETE_ID_CANNOT_BE_NULL);
        Objects.requireNonNull(trainerId, TRAINER_ID_CANNOT_BE_NULL);

        return trainerId.equals(athleteToTrainer.get(athleteId));
    }

    @Override
    public List<String> findAthleteIdsByTrainerId(String trainerId) {
        Objects.requireNonNull(trainerId, TRAINER_ID_CANNOT_BE_NULL);

        List<String> athletes = trainerToAthletes.get(trainerId);

        if (athletes == null || athletes.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(athletes);
    }

    @Override
    public Optional<String> findTrainerIdByAthleteId(String athleteId) {
        Objects.requireNonNull(athleteId, ATHLETE_ID_CANNOT_BE_NULL);

        return Optional.ofNullable(athleteToTrainer.get(athleteId));
    }

}
