package com.example.fitplannerserver.dao.filesystem;

import com.example.fitplannerserver.dao.CoachingDao;
import com.example.fitplannerserver.exception.DaoException;

import java.util.List;
import java.util.Optional;

public class FileSystemCoachingDao implements CoachingDao {
    @Override
    public void linkAthleteToTrainer(String athleteUuid, String trainerUuid) throws DaoException {

    }

    @Override
    public void unlink(String athleteId, String trainerId) throws DaoException {

    }

    @Override
    public boolean isClientOf(String trainerId, String athleteId) throws DaoException {
        return false;
    }

    @Override
    public List<String> findAthleteIdsByTrainerId(String trainerId) throws DaoException {
        return List.of();
    }

    @Override
    public Optional<String> findTrainerIdByAthleteId(String athleteId) throws DaoException {
        return Optional.empty();
    }
}
