package com.example.fitplannerclient.ui.fx;

import com.example.fitplannerclient.AppControllerFactory;
import com.example.fitplannerclient.controller.AuthManager;
import com.example.fitplannerclient.service.SessionManager;
import com.example.fitplannerclient.ui.fx.guicontroller.AuthenticationViewController;
import com.example.fitplannerclient.ui.fx.guicontroller.HomeViewController;
import javafx.application.Platform;

public class Navigator {

    private final GuiManager guiManager;
    private final AppControllerFactory appControllerFactory;
    private final SessionManager sessionManager;

    private GuiController currentGuiController;

    public Navigator(GuiManager guiManager, AppControllerFactory factory, SessionManager sessionManager) {
        this.guiManager = guiManager;
        this.appControllerFactory = factory;
        this.sessionManager = sessionManager;
    }

    /**
     * Centralized method to handle view transitions safely.
     */
    private void navigateTo(GuiController nextController) {
        if (currentGuiController != null) {
            currentGuiController.stop();
        }
        currentGuiController = nextController;
        guiManager.setView(nextController.getView());
        nextController.start();
    }

    public void requireAuthentication(Runnable onSuccess) {
        AuthManager authAppController = appControllerFactory.createAuthManager();

        Runnable finalSuccessAction = (onSuccess != null) ? onSuccess : this::startHomeController;

        AuthenticationViewController authGuiController = new AuthenticationViewController(
                this,
                guiManager,
                authAppController,
                finalSuccessAction
        );

        Platform.runLater(() -> navigateTo(authGuiController));
    }

    public void requireAuthenticationOverlay(Runnable onSuccess) {
        AuthManager authAppController = appControllerFactory.createAuthManager();

        // quando il login ha successo, nascondo l'overlay ed eseguo onSuccess
        Runnable onLoginSuccess = () -> {
            guiManager.hideOverlay();
            if (onSuccess != null) {
                onSuccess.run();
            }
        };

        AuthenticationViewController authGuiController = new AuthenticationViewController(
                this, guiManager, authAppController, onLoginSuccess
        );

        guiManager.showOverlay(authGuiController.getView());
        authGuiController.start();
    }

    public void startHomeController() {
        if (!sessionManager.isLoggedIn()) {
            requireAuthentication(this::startHomeController);
        } else {
            HomeViewController homeController = new HomeViewController();
            Platform.runLater(() -> navigateTo(homeController));
        }
    }
}
