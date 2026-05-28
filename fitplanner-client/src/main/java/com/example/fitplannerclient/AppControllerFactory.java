package com.example.fitplannerclient;

import com.example.fitplannerclient.controller.AuthManager;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.service.api.*;

public class AppControllerFactory {

    private final AuthApi authApi;
    private final ProfileApi profileApi;
    private final ExerciseLibraryApi exerciseLibraryApi;
    private final WorkoutPlanApi workoutPlanApi;
    private final SessionLogApi sessionLogApi;

    private AuthManager authManager;
    private ProfileManager profileManager;
    private ExerciseLibraryManager exerciseLibraryManager;
    private WorkoutPlanManager workoutPlanManager;

    public AppControllerFactory(
            AuthApi authApi,
            ProfileApi profileApi,
            ExerciseLibraryApi exerciseLibraryApi,
            WorkoutPlanApi workoutPlanApi,
            SessionLogApi sessionLogApi
    ) {
        this.authApi = authApi;
        this.profileApi = profileApi;
        this.exerciseLibraryApi = exerciseLibraryApi;
        this.workoutPlanApi = workoutPlanApi;
        this.sessionLogApi = sessionLogApi;
    }

    public AuthManager createAuthManager() {
        if (authManager == null) {
            authManager = new AuthManager(authApi);
        }
        return authManager;
    }

    public ProfileManager createProfileManager() {
        if (profileManager == null) {
            profileManager = new ProfileManager(profileApi);
        }
        return profileManager;
    }

    public ExerciseLibraryManager createExerciseLibraryManager() {
        if (exerciseLibraryManager == null) {
            exerciseLibraryManager = new ExerciseLibraryManager(exerciseLibraryApi);
        }
        return exerciseLibraryManager;
    }

    public WorkoutPlanManager createWorkoutPlanManager() {
        if (workoutPlanManager == null) {
            workoutPlanManager = new WorkoutPlanManager(workoutPlanApi);
        }
        return workoutPlanManager;
    }

    public SessionLogApi createSessionLogApi() {
        return sessionLogApi;
    }

    public void resetManagers() {
        this.authManager = null;
        this.profileManager = null;
        this.exerciseLibraryManager = null;
        this.workoutPlanManager = null;
    }

    public void resetDataManagers() {
        this.exerciseLibraryManager = null;
        this.workoutPlanManager = null;
    }

    public com.example.fitplannerclient.controller.log.WorkoutHistoryManager createWorkoutHistoryManager() {
        return new com.example.fitplannerclient.controller.log.WorkoutHistoryManager(sessionLogApi);
    }
}
