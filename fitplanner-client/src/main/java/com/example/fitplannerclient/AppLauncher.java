package com.example.fitplannerclient;

import com.example.fitplannerclient.config.ConfigurationManager;
import com.example.fitplannerclient.exception.ConfigException;
import com.example.fitplannerclient.service.HttpService;
import com.example.fitplannerclient.service.SessionManager;
import com.example.fitplannerclient.service.api.*;
import com.example.fitplannerclient.ui.cli.CliEngine;
import javafx.application.Application;

import java.util.concurrent.CompletableFuture;

public class AppLauncher {

    public static void main(String[] args) {
        try {
            ConfigurationManager configManager = new ConfigurationManager();
            
            if (configManager.getTypeOfUI() == ConfigurationManager.UiType.CLI) {
                System.setProperty("org.slf4j.simpleLogger.logFile", "cli.log");
            }
            
            SessionManager sessionManager = new SessionManager();
            String apiUrl = configManager.getApiUrl();

            HttpService httpService = new HttpService(apiUrl, sessionManager, () -> {
                CompletableFuture<Boolean> future = new CompletableFuture<>();
                if (configManager.getTypeOfUI() == ConfigurationManager.UiType.JAVAFX && JavaFxApp.onUnauthorized != null) {
                    JavaFxApp.onUnauthorized.accept(future);
                } else {
                    System.out.println("\nSessione scaduta o non autorizzato. Ritorno al login...");
                    future.complete(false);
                }
                return future;
            });

            AuthApi authApi = new HttpAuthApi(httpService, sessionManager);
            ProfileApi profileApi = new HttpProfileApi(httpService);
            ExerciseLibraryApi exerciseLibraryApi = new HttpExerciseLibraryApi(httpService);
            WorkoutPlanApi workoutPlanApi = new HttpWorkoutPlanApi(httpService);
            SessionLogApi sessionLogApi = new HttpSessionLogApi(httpService);

            AppControllerFactory factory = new AppControllerFactory(
                    authApi, profileApi, exerciseLibraryApi, workoutPlanApi, sessionLogApi
            );

            switch (configManager.getTypeOfUI()) {
                case CLI -> {
                    CliEngine cliEngine = new CliEngine(factory, sessionManager);
                    cliEngine.start();
                }
                case JAVAFX -> {
                    JavaFxApp.factory = factory;
                    JavaFxApp.sessionManager = sessionManager;
                    Application.launch(JavaFxApp.class, args);
                }
            }
        } catch (ConfigException e) {
            System.err.println("Configuration error: " + e.getMessage());
            System.exit(1);
        }

    }
}
