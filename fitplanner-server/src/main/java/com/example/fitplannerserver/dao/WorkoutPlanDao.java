package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.WorkoutPlan;

import java.util.List;
import java.util.Optional;

public interface WorkoutPlanDao {

    // Creates a new empty workout plan linked to the trainer who authored it
    void savePlan(WorkoutPlan plan) throws DaoException;

    // Completely deletes a plan and cascades to its sessions
    void deletePlan(String planId) throws DaoException;

    // Retrieves a specific plan by its ID
    Optional<WorkoutPlan> findPlanById(String planId) throws DaoException;

    // Retrieves the active plan assigned to an athlete
    Optional<WorkoutPlan> findAssignedPlanByAthleteId(String athleteId) throws DaoException;

    // Retrieves all plans authored by a specific trainer
    List<WorkoutPlan> findPlansByTrainerId(String trainerId) throws DaoException;

}