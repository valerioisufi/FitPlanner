package com.example.fitplannerclient;

import com.example.fitplannerclient.controller.AuthManager;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.service.facade.*;

public class AppControllerFactory {

    private final AuthFacade authFacade;
    private final ProfileFacade profileFacade;
    private final ExerciseLibraryFacade exerciseLibraryFacade;
    private final WorkoutPlanFacade workoutPlanFacade;
    private final SessionLogFacade sessionLogFacade;

    private AuthManager authManager;
    private ProfileManager profileManager;
    private ExerciseLibraryManager exerciseLibraryManager;
    private WorkoutPlanManager workoutPlanManager;

    public AppControllerFactory(
            AuthFacade authFacade,
            ProfileFacade profileFacade,
            ExerciseLibraryFacade exerciseLibraryFacade,
            WorkoutPlanFacade workoutPlanFacade,
            SessionLogFacade sessionLogFacade
    ) {
        this.authFacade = authFacade;
        this.profileFacade = profileFacade;
        this.exerciseLibraryFacade = exerciseLibraryFacade;
        this.workoutPlanFacade = workoutPlanFacade;
        this.sessionLogFacade = sessionLogFacade;
    }

    public AuthManager createAuthManager() {
        if (authManager == null) {
            authManager = new AuthManager(authFacade);
        }
        return authManager;
    }

    public ProfileManager createProfileManager() {
        if (profileManager == null) {
            profileManager = new ProfileManager(profileFacade);
        }
        return profileManager;
    }

    public ExerciseLibraryManager createExerciseLibraryManager() {
        if (exerciseLibraryManager == null) {
            exerciseLibraryManager = new ExerciseLibraryManager(exerciseLibraryFacade);
        }
        return exerciseLibraryManager;
    }

    public WorkoutPlanManager createWorkoutPlanManager() {
        if (workoutPlanManager == null) {
            workoutPlanManager = new WorkoutPlanManager(workoutPlanFacade);
        }
        return workoutPlanManager;
    }

    public SessionLogFacade createSessionLogFacade() {
        return sessionLogFacade;
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
        return new com.example.fitplannerclient.controller.log.WorkoutHistoryManager(sessionLogFacade);
    }
}
