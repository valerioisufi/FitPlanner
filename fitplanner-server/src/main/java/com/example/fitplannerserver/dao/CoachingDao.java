package com.example.fitplannerserver.dao;

import java.util.List;

public interface CoachingDao {
    // Links an athlete to a trainer
    void linkAthleteToTrainer(String athleteUuid, String trainerUuid);

    // Unlinks them (important feature for the future!)
    void unlink(String athleteUuid, String trainerUuid);

    // Checks if a specific relationship exists
    boolean isClientOf(String trainerUuid, String athleteUuid);

    // Returns the list of athlete IDs followed by a trainer
    List<String> findAthleteIdsByTrainerId(String trainerUuid);

    // Returns the trainer ID for a given athlete
    String findTrainerIdByAthleteId(String athleteUuid);
}