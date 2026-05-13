package com.example.fitplannerclient.ui.fx;

import com.example.fitplannerclient.AppControllerFactory;
import com.example.fitplannerclient.controller.AuthManager;
import com.example.fitplannerclient.service.SessionManager;
import com.example.fitplannerclient.ui.fx.guicontroller.AuthenticationController;
import com.example.fitplannerclient.ui.fx.guicontroller.HomeController;
import javafx.application.Platform;

public class Navigator {

    private final GuiManager guiManager;
    private final AppControllerFactory appControllerFactory;
    private final SessionManager sessionManager;

    public Navigator(GuiManager guiManager, AppControllerFactory factory, SessionManager sessionManager) {
        this.guiManager = guiManager;
        this.appControllerFactory = factory;
        this.sessionManager = sessionManager;
    }

    public void requireAuthentication() {
        AuthManager authAppController = appControllerFactory.createAuthManager();

        AuthenticationController authGuiController = new AuthenticationController(this, authAppController);

        Platform.runLater(() -> authGuiController.start(guiManager));
    }

    public void startHomeController() {
        if (!sessionManager.isLoggedIn()) {
            requireAuthentication();
        } else {
            // Assume you have a createHomeManager() in your factory
            HomeController homeGuiController = new HomeController();
            Platform.runLater(() -> homeGuiController.start(guiManager));
        }
    }

}
