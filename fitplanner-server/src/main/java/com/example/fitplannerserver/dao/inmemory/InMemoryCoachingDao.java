package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.CoachingDao;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryCoachingDao implements CoachingDao {

    // Map: trainerId -> List of athleteIds
    private final Map<String, List<String>> trainerToAthletes = new ConcurrentHashMap<>();

    // Map: athleteId -> trainerId
    private final Map<String, String> athleteToTrainer = new ConcurrentHashMap<>();

    @Override
    public synchronized void linkAthleteToTrainer(String athleteId, String trainerId) {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

        // If the athlete already has a different trainer
        String oldTrainerId = athleteToTrainer.get(athleteId);
        if (oldTrainerId != null && !oldTrainerId.equals(trainerId)) {
            unlink(athleteId, oldTrainerId);
        }

        trainerToAthletes.computeIfAbsent(trainerId, k -> new CopyOnWriteArrayList<>()).add(athleteId);

        athleteToTrainer.put(athleteId, trainerId);
    }

    @Override
    public synchronized void unlink(String athleteId, String trainerId) {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

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
        Objects.requireNonNull(athleteId, "athleteId cannot be null");
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

        return trainerId.equals(athleteToTrainer.get(athleteId));
    }

    @Override
    public List<String> findAthleteIdsByTrainerId(String trainerId) {
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

        List<String> athletes = trainerToAthletes.get(trainerId);

        if (athletes == null || athletes.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(athletes);
    }

    @Override
    public Optional<String> findTrainerIdByAthleteId(String athleteId) {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");

        return Optional.ofNullable(athleteToTrainer.get(athleteId));
    }

}
