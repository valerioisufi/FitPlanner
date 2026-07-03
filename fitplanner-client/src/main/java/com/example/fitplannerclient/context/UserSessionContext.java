package com.example.fitplannerclient.context;

import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.controller.log.WorkoutHistoryManager;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.controller.plan.editor.EditWorkoutPlanManager;
import com.example.fitplannerclient.controller.plan.execution.WorkoutExecutionManager;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.entity.profile.Profile;
import com.example.fitplannerclient.repository.ExerciseRepository;
import com.example.fitplannerclient.repository.ProfileRepository;
import com.example.fitplannerclient.repository.SessionLogRepository;
import com.example.fitplannerclient.repository.WorkoutPlanRepository;
import com.example.fitplannerclient.service.api.ExerciseLibraryApi;
import com.example.fitplannerclient.service.api.ProfileApi;
import com.example.fitplannerclient.service.api.SessionLogApi;
import com.example.fitplannerclient.service.api.WorkoutPlanApi;

import java.util.concurrent.CompletableFuture;

/**
 * Contenitore dello stato specifico dell'utente: vive dal login al logout.
 * Gestisce le classi repository (le cache dei dati dell'utente corrente) e l'identità dell'utente.
 * Al logout l'intero contesto viene scartato.
 * I Manager sono façade stateless e vengono creati a ogni richiesta.
 */
public class UserSessionContext {

    private final ProfileRepository profileRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutPlanRepository workoutPlanRepository;
    private final SessionLogRepository sessionLogRepository;

    UserSessionContext(
            ProfileApi profileApi,
            ExerciseLibraryApi exerciseLibraryApi,
            WorkoutPlanApi workoutPlanApi,
            SessionLogApi sessionLogApi
    ) {
        this.profileRepository = new ProfileRepository(profileApi);
        this.exerciseRepository = new ExerciseRepository(exerciseLibraryApi);
        this.workoutPlanRepository = new WorkoutPlanRepository(workoutPlanApi);
        this.sessionLogRepository = new SessionLogRepository(sessionLogApi);
    }

    /**
     * Identità dell'utente di questa sessione.
     * Valida per costruzione: il SessionManager espone il contesto solo dopo
     * aver caricato con successo il profilo.
     */
    public String getUserId() {
        return profileRepository.getCachedProfile().getUserId();
    }

    public CompletableFuture<Profile> loadProfileAsync() {
        return profileRepository.getProfileInfoAsync();
    }

    public ProfileManager createProfileManager() {
        return new ProfileManager(profileRepository);
    }

    public ExerciseLibraryManager createExerciseLibraryManager() {
        return new ExerciseLibraryManager(exerciseRepository);
    }

    public WorkoutPlanManager createWorkoutPlanManager() {
        return new WorkoutPlanManager(workoutPlanRepository, exerciseRepository);
    }

    public EditWorkoutPlanManager createEditWorkoutPlanManager() {
        return new EditWorkoutPlanManager(workoutPlanRepository, exerciseRepository);
    }

    public WorkoutHistoryManager createWorkoutHistoryManager() {
        return createWorkoutHistoryManagerFor(getUserId());
    }

    public WorkoutHistoryManager createWorkoutHistoryManagerFor(String athleteId) {
        return new WorkoutHistoryManager(sessionLogRepository, athleteId);
    }

    public WorkoutExecutionManager createWorkoutExecutionManager() {
        return new WorkoutExecutionManager(workoutPlanRepository, sessionLogRepository, exerciseRepository);
    }
}
