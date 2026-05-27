package com.example.fitplannerserver.dao.database;

import com.example.fitplannerserver.dao.WorkoutPlanDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.WorkoutPlan;

import java.util.List;
import java.util.Optional;

public class DatabaseWorkoutPlanDao implements WorkoutPlanDao {
    @Override
    public void savePlan(WorkoutPlan plan) throws DaoException {

    }

    @Override
    public void deletePlan(String planId) throws DaoException {

    }

    @Override
    public Optional<WorkoutPlan> findPlanById(String planId) throws DaoException {
        return Optional.empty();
    }

    @Override
    public Optional<WorkoutPlan> findAssignedPlanByAthleteId(String athleteId) throws DaoException {
        return Optional.empty();
    }

    @Override
    public List<WorkoutPlan> findPlansByTrainerId(String trainerId) throws DaoException {
        return List.of();
    }
}
