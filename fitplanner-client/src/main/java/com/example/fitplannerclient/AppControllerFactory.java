package com.example.fitplannerclient;

import com.example.fitplannerclient.controller.AuthManager;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.controller.log.WorkoutHistoryManager;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.controller.plan.WorkoutPlanRepository;
import com.example.fitplannerclient.controller.plan.editor.EditWorkoutPlanManager;
import com.example.fitplannerclient.controller.plan.execution.WorkoutExecutionManager;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.repository.ExerciseRepository;
import com.example.fitplannerclient.repository.ProfileRepository;
import com.example.fitplannerclient.repository.SessionLogRepository;
import com.example.fitplannerclient.service.api.*;

public class AppControllerFactory {

    private final AuthApi authApi;
    private final ProfileApi profileApi;
    private final ExerciseLibraryApi exerciseLibraryApi;
    private final WorkoutPlanApi workoutPlanApi;
    private final SessionLogApi sessionLogApi;

    // i Repository sono cachati (possiedono lo stato condiviso);
    // i Manager vengono creati a ogni richiesta (façade stateless)
    private AuthManager authManager;
    private ProfileRepository profileRepository;
    private ExerciseRepository exerciseRepository;
    private WorkoutPlanRepository workoutPlanRepository;
    private SessionLogRepository sessionLogRepository;

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

    public ExerciseRepository createExerciseRepository() {
        if (exerciseRepository == null) {
            exerciseRepository = new ExerciseRepository(exerciseLibraryApi);
        }
        return exerciseRepository;
    }

    public WorkoutPlanRepository createWorkoutPlanRepository() {
        if (workoutPlanRepository == null) {
            workoutPlanRepository = new WorkoutPlanRepository(workoutPlanApi);
        }
        return workoutPlanRepository;
    }

    public SessionLogRepository createSessionLogRepository() {
        if (sessionLogRepository == null) {
            sessionLogRepository = new SessionLogRepository(sessionLogApi);
        }
        return sessionLogRepository;
    }

    public ProfileManager createProfileManager() {
        return new ProfileManager(profileApi, createProfileRepository());
    }

    public ExerciseLibraryManager createExerciseLibraryManager() {
        return new ExerciseLibraryManager(createExerciseRepository());
    }

    public WorkoutPlanManager createWorkoutPlanManager() {
        return new WorkoutPlanManager(createWorkoutPlanRepository(), workoutPlanApi, createExerciseRepository());
    }

    public EditWorkoutPlanManager createEditWorkoutPlanManager() {
        return new EditWorkoutPlanManager(createWorkoutPlanRepository(), createExerciseRepository());
    }

    public WorkoutHistoryManager createWorkoutHistoryManager() {
        return new WorkoutHistoryManager(createSessionLogRepository());
    }

    public WorkoutExecutionManager createWorkoutExecutionManager() {
        return new WorkoutExecutionManager(createWorkoutPlanRepository(), sessionLogApi, createExerciseRepository());
    }

    /**
     * Logout: invalida la sessione e tutte le cache dati per-utente.
     * profileRepository è tenuto di proposito: mantiene l'identità corrente e alimenta didUserChange().
     */
    public void resetManagers() {
        this.authManager = null;
        this.exerciseRepository = null;
        this.workoutPlanRepository = null;
        this.sessionLogRepository = null;
    }

    /**
     * Cambio utente: scarta le cache dati del precedente
     * (profileRepository resta: contiene la nuova identità appena caricata).
     */
    public void resetDataManagers() {
        this.exerciseRepository = null;
        this.workoutPlanRepository = null;
        this.sessionLogRepository = null;
    }
}
