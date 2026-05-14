package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.model.plan.WorkoutPlan;
import com.example.fitplannerserver.model.plan.WorkoutSession;
import java.util.List;

public interface WorkoutPlanDao {

    // --- WORKOUT PLAN OPERATIONS ---

    // Creates a new empty workout plan linked to the trainer who authored it
    void savePlan(WorkoutPlan plan);

    // Updates the general content (title, notes) of an existing plan
    void updatePlan(WorkoutPlan plan);

    // Completely deletes a plan and cascades to its sessions
    void deletePlan(String planUuid);

    // Retrieves a specific plan by its ID
    WorkoutPlan findPlanById(String planUuid);

    // Assigns an existing plan to an athlete
    void assignPlanToAthlete(String planUuid, String athleteUuid);

    // Retrieves the active plan assigned to an athlete
    WorkoutPlan findAssignedPlanByAthleteId(String athleteUuid);

    // Retrieves all plans authored by a specific trainer
    List<WorkoutPlan> findPlansByTrainerId(String trainerUuid);


    // --- WORKOUT SESSION OPERATIONS ---

    // Adds a new session to a specific plan
    void saveSession(String planUuid, WorkoutSession session);

    // Updates an existing session
    void updateSession(WorkoutSession session);

    // Deletes a specific session
    void deleteSession(String sessionUuid);

    // Retrieves a session by its ID
    WorkoutSession findSessionById(String sessionUuid);
}