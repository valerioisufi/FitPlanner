package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.model.log.SessionLog;

import java.util.List;

public interface SessionLogDao {
    // Retrieves a complete session log by its ID
    SessionLog findSessionLogById(String sessionUuid);

    // Saves or updates a full session log (exercises, reps, weights)
    void saveSessionLog(SessionLog log);

    // Retrieves logs for a specific athlete within a time range
    List<SessionLog> findLogsByAthleteIdAndDateRange(String athleteUuid, long startTimestamp, long endTimestamp);

    SessionLog findMostRecentSessionContainingExercise(
            String athleteUuid,
            String exerciseUuid
    );

}