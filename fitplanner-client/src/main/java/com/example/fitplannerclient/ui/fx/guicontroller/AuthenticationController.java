package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.controller.AuthManager;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.AuthenticationView;
import com.example.fitplannercommon.LoginBean;
import com.example.fitplannercommon.ProfileBean;
import com.example.fitplannercommon.RegisterBean;
import javafx.application.Platform; // Import cruciale per JavaFX

public class AuthenticationController implements GuiController {
    private GuiManager guiManager;

    // We keep a reference to the Navigator to change screens
    private final Navigator navigator;
    private final AuthManager authManager;
    private final AuthenticationView view;

    // Inject both Navigator and AuthManager via constructor
    public AuthenticationController(Navigator navigator, AuthManager authManager) {
        this.navigator = navigator;
        this.authManager = authManager;

        this.view = new AuthenticationView();

        this.view.setLoginBtnAction(() -> this.onLogin(this.view.getUsername(), this.view.getPassword()));
        this.view.setRegistrationBtnAction(() -> this.onRegister(this.view.getUsername(), this.view.getPassword()));
    }

    private void onLogin(String username, String password) {
        var loginBean = new LoginBean(username, password);

        // Delegate to the injected AuthManager, not the non-existent authFacade
        authManager.loginAsync(loginBean)
                .thenRun(() -> {
                    // Switch back to the JavaFX Application Thread to update the UI
                    Platform.runLater(() -> {
                        this.guiManager.showNotification("Login avvenuto con successo!");
                        this.navigator.startHomeController(); // Use the injected navigator
                    });
                })
                .exceptionally(ex -> {
                    // UI updates must always be on the FX Thread, even for errors
                    Platform.runLater(() -> {
                        this.guiManager.showNotification(ex.getCause().getMessage());
                    });
                    return null;
                });
    }

    private void onRegister(String username, String password) {
        var registerBean = new RegisterBean(username, password, new ProfileBean());

        authManager.registerAsync(registerBean)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        this.guiManager.showNotification("Registrazione avvenuta con successo!");
                        this.navigator.startHomeController();
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        this.guiManager.showNotification(ex.getCause().getMessage());
                    });
                    return null;
                });
    }

    @Override
    public void start(GuiManager guiManager) {
        this.guiManager = guiManager;
        this.guiManager.setView(view);
    }
}