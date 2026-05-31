package com.example.fitplannerclient;

import com.example.fitplannerclient.controller.AuthManager;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.controller.log.WorkoutHistoryManager;
import com.example.fitplannerclient.controller.plan.editor.EditWorkoutPlanManager;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.controller.plan.WorkoutPlanRepository;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.repository.ExerciseRepository;
import com.example.fitplannerclient.repository.ProfileRepository;
import com.example.fitplannerclient.service.api.*;

public class AppControllerFactory {

    private final AuthApi authApi;
    private final ProfileApi profileApi;
    private final ExerciseLibraryApi exerciseLibraryApi;
    private final WorkoutPlanApi workoutPlanApi;
    private final SessionLogApi sessionLogApi;

    private AuthManager authManager;
    private ProfileRepository profileRepository;
    private ProfileManager profileManager;
    private ExerciseRepository exerciseRepository;
    private ExerciseLibraryManager exerciseLibraryManager;
    private WorkoutPlanManager workoutPlanManager;
    private WorkoutPlanRepository workoutPlanRepository;
    private EditWorkoutPlanManager editWorkoutPlanManager;

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

    public ProfileRepository createProfileRepository() {
        if (profileRepository == null) {
            profileRepository = new ProfileRepository(profileApi);
        }
        return profileRepository;
    }

    public ProfileManager createProfileManager() {
        return new ProfileManager(profileApi, createProfileRepository());
    }

    public ExerciseRepository createExerciseRepository() {
        if (exerciseRepository == null) {
            exerciseRepository = new ExerciseRepository(exerciseLibraryApi);
        }
        return exerciseRepository;
    }

    public ExerciseLibraryManager createExerciseLibraryManager() {
        return new ExerciseLibraryManager(createExerciseRepository());
    }

    public WorkoutPlanManager createWorkoutPlanManager() {
        return new WorkoutPlanManager(workoutPlanApi);
    }

    public WorkoutPlanRepository createWorkoutPlanRepository() {
        if (workoutPlanRepository == null) {
            workoutPlanRepository = new WorkoutPlanRepository(workoutPlanApi, createExerciseRepository());
        }
        return workoutPlanRepository;
    }

    public EditWorkoutPlanManager createEditWorkoutPlanManager() {
        return editWorkoutPlanManager = new EditWorkoutPlanManager(createWorkoutPlanRepository());
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
        this.workoutPlanRepository = null;
    }

    public WorkoutHistoryManager createWorkoutHistoryManager() {
        return new WorkoutHistoryManager(sessionLogApi);
    }
}
