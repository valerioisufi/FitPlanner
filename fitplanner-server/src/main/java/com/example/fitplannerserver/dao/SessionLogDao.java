package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.log.SessionLog;

import java.util.List;
import java.util.Optional;

public interface SessionLogDao {

    // Saves a new session log (exercises, reps, weights)
    void saveSessionLog(SessionLog log) throws DaoException;

    // Retrieves logs for a specific athlete within a time range; no ordering is guaranteed
    List<SessionLog> findLogsByAthleteIdAndDateRange(String athleteId, long startTimestamp, long endTimestamp) throws DaoException;

    Optional<SessionLog> findMostRecentSessionContainingExercise(
            String athleteId,
            String exerciseUuid
    ) throws DaoException;

}