package com.example.fitplannerclient;

import com.example.fitplannerclient.config.ConfigurationManager;
import com.example.fitplannerclient.service.*;
import com.example.fitplannerclient.service.facade.*;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AppLauncher extends Application {

    private Navigator navigator;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("FitPlanner");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app_icon.png"))));

        // 1. Initialize Configuration
        ConfigurationManager configManager = new ConfigurationManager();

        // 2. Initialize Core Services
        SessionManager sessionManager = new SessionManager();

        HttpService httpService = new HttpService(configManager.getApiUrl(), sessionManager, () -> {
            CompletableFuture<Boolean> manualLoginFuture = new CompletableFuture<>();
            Platform.runLater(() -> {
                this.navigator.requireAuthenticationOverlay(() -> manualLoginFuture.complete(true));
            });
            return manualLoginFuture;
        });

        AuthFacade authFacade = new AuthFacade(httpService, sessionManager);
        ProfileFacade profileFacade = new ProfileFacade(httpService);
        ExerciseLibraryFacade exerciseLibraryFacade = new ExerciseLibraryFacade(httpService);
        WorkoutPlanFacade workoutPlanFacade = new WorkoutPlanFacade(httpService);
        SessionLogFacade sessionLogFacade = new SessionLogFacade(httpService);

        AppControllerFactory factory = new AppControllerFactory(
                authFacade, profileFacade, exerciseLibraryFacade, workoutPlanFacade, sessionLogFacade
        );

        GuiManager guiManager = new GuiManager(stage);
        this.navigator = new Navigator(guiManager, factory, sessionManager);

        // 3. Start the flow
//        sessionManager.logout();
        this.navigator.startHomeController();

        stage.show();
    }
}