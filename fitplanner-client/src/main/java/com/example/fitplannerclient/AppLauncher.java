package com.example.fitplannerclient;

import com.example.fitplannerclient.service.AuthFacade;
import com.example.fitplannerclient.service.HttpService;
import com.example.fitplannerclient.service.SessionManager;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class AppLauncher extends Application {

    private Navigator navigator;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("FitPlanner");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app_icon.png"))));

        SessionManager sessionManager = new SessionManager();

        HttpService httpService = new HttpService(sessionManager, () -> {
            // If network fails to refresh token, force UI to go to login
            Platform.runLater(() -> this.navigator.requireAuthentication());
        });

        AuthFacade authFacade = new AuthFacade(httpService, sessionManager);
        AppControllerFactory factory = new AppControllerFactory(authFacade);

        GuiManager guiManager = new GuiManager(stage);
        this.navigator = new Navigator(guiManager, factory, sessionManager);

        // Start the flow!
        this.navigator.startHomeController();

        stage.show();
    }
}