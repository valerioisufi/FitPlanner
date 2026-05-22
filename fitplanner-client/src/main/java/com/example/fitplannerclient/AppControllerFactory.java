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
        return new AuthManager(authFacade);
    }

    public ProfileManager createProfileManager() {
        return new ProfileManager(profileFacade);
    }

    public ExerciseLibraryManager createExerciseLibraryManager() {
        return new ExerciseLibraryManager(exerciseLibraryFacade);
    }

    public WorkoutPlanManager createWorkoutPlanManager() {
        return new WorkoutPlanManager(workoutPlanFacade, sessionLogFacade);
    }

    public SessionLogFacade createSessionLogFacade() {
        return sessionLogFacade;
    }
}
