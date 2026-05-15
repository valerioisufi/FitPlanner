package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.exception.DaoException;

import java.util.List;
import java.util.Optional;

public interface CoachingDao {
    // Links an athlete to a trainer
    void linkAthleteToTrainer(String athleteUuid, String trainerUuid);

    // Unlinks them
    void unlink(String athleteId, String trainerId) throws DaoException;

    // Checks if a specific relationship exists
    boolean isClientOf(String trainerId, String athleteId) throws DaoException;

    // Returns the list of athlete IDs followed by a trainer
    List<String> findAthleteIdsByTrainerId(String trainerId) throws DaoException;

    // Returns the trainer ID for a given athlete
    Optional<String> findTrainerIdByAthleteId(String athleteId) throws DaoException;
}