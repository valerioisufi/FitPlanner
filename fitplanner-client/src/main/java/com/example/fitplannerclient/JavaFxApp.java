package com.example.fitplannerclient;

import com.example.fitplannerclient.config.ConfigurationManager;
import com.example.fitplannerclient.exception.ConfigException;
import com.example.fitplannerclient.service.*;
import com.example.fitplannerclient.service.api.*;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class JavaFxApp extends Application {

    public static AppControllerFactory factory;
    public static SessionManager sessionManager;
    public static Consumer<CompletableFuture<Boolean>> onUnauthorized;

    private Navigator navigator;

    @Override
    public void start(Stage stage) {
        stage.setTitle("FitPlanner");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app_icon.png"))));

        try {
            // Set the 401 Unauthorized callback
            JavaFxApp.onUnauthorized = (future) -> {
                Platform.runLater(() -> {
                    this.navigator.requireAuthenticationOverlay(() -> future.complete(true));
                });
            };

            GuiManager guiManager = new GuiManager(stage);
            this.navigator = new Navigator(guiManager, factory, sessionManager);

            // Start the flow
            this.navigator.goHome();

            stage.show();
        } catch (ConfigException configException) {
            System.err.println("Errore durante la lettura della configurazione: " + configException.getMessage());
            System.exit(1);
        }
    }

}