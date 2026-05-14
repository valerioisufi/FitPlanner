package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.WorkoutSessionBean;
import com.example.fitplannerserver.security.IdentityProvider;

public class EditWorkoutPlanController {

    private final IdentityProvider identityProvider;

    public EditWorkoutPlanController(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    public void assignPlanTo(String planUuid, String email) {
        String trainerId = identityProvider.getUserId();
        // Verify trainer privileges and assign plan to the athlete via DAO
    }

    public void updatePlan(String planUuid, String content) {
        // Validation and update logic
    }

    public void deletePlan(String planUuid) {
        // Deletion logic
    }

    public void addSession(String planUuid, WorkoutSessionBean sessionBean) {
        // Business logic to attach a session to a workout plan
    }

    public void updateSession(String sessionUuid, WorkoutSessionBean sessionBean) {
        // Business logic to update a session
    }

    public void deleteSession(String sessionUuid) {
        // Business logic to delete a session
    }
}
