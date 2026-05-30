package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.SessionLogDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.log.SessionLog;

import java.util.List;
import java.util.Optional;

public class FileSystemSessionLogDao implements SessionLogDao {
    @Override
    public void saveSessionLog(SessionLog log) throws DaoException {

    }

    @Override
    public List<SessionLog> findLogsByAthleteIdAndDateRange(String athleteId, long startTimestamp, long endTimestamp) throws DaoException {
        return List.of();
    }

    @Override
    public Optional<SessionLog> findMostRecentSessionContainingExercise(String athleteId, String exerciseUuid) throws DaoException {
        return Optional.empty();
    }
}
