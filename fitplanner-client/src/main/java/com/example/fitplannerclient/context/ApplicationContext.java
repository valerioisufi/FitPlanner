package com.example.fitplannerclient.context;

import com.example.fitplannerclient.controller.AuthManager;
import com.example.fitplannerclient.controller.SessionController;
import com.example.fitplannerclient.service.HttpService;
import com.example.fitplannerclient.service.TokenStore;
import com.example.fitplannerclient.service.api.*;

import java.util.concurrent.CompletableFuture;

/**
 * Composition root del client: vive quanto il processo.
 * Possiede i servizi condivisi (token store, http, Api) e il SessionController.
 * Lo stato per-utente non abita qui: sta negli UserSessionContext creati a ogni login.
 */
public class ApplicationContext {

    private final TokenStore tokenStore;

    private final ProfileApi profileApi;
    private final ExerciseLibraryApi exerciseLibraryApi;
    private final WorkoutPlanApi workoutPlanApi;
    private final SessionLogApi sessionLogApi;

    private final SessionController sessionController;

    public ApplicationContext(String apiUrl) {
        this.tokenStore = new TokenStore();

        HttpService httpService = new HttpService(apiUrl, tokenStore, this::handleSessionExpired);

        AuthApi authApi = new HttpAuthApi(httpService, tokenStore);
        this.profileApi = new HttpProfileApi(httpService);
        this.exerciseLibraryApi = new HttpExerciseLibraryApi(httpService);
        this.workoutPlanApi = new HttpWorkoutPlanApi(httpService);
        this.sessionLogApi = new HttpSessionLogApi(httpService);

        this.sessionController = new SessionController(new AuthManager(authApi), tokenStore, this::createUserSession);
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    private UserSessionContext createUserSession() {
        return new UserSessionContext(profileApi, exerciseLibraryApi, workoutPlanApi, sessionLogApi);
    }

    private CompletableFuture<Boolean> handleSessionExpired() {
        return sessionController.onSessionExpired();
    }
}
