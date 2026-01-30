package com.example.fitplannerclient.ui.gui1;

import com.example.fitplannerclient.Navigator;
import com.example.fitplannerclient.service.AuthenticationBoundary;
import com.example.fitplannerclient.ui.GraphicController;
import com.example.fitplannerclient.ui.gui1.view.AuthenticationView;
import com.example.fitplannercommon.LoginBean;
import com.example.fitplannercommon.RegisterBean;

public class AuthenticationController implements GraphicController {
    private GuiManager guiManager;

    private AuthenticationBoundary authenticationBoundary;
    private AuthenticationView view;

    public AuthenticationController(AuthenticationBoundary authenticationBoundary) {
        this.authenticationBoundary = authenticationBoundary;

        view = new AuthenticationView();

        view.setLoginBtnAction(() -> this.onLogin(this.view.getUsername(), this.view.getPassword()));
        view.setRegistrationBtnAction(() -> this.onRegister(this.view.getUsername(), this.view.getPassword()));
    }

    private void onLogin(String username, String password) {
        var loginBean = new LoginBean(username, password);

        authenticationBoundary.loginAsync(loginBean)
                .thenRun(() -> {
                    // Login successful, proceed to the next screen
                    this.guiManager.showNotification("Login successful!");
                    Navigator.getInstance().startHomeController();
                })
                .exceptionally(ex -> {
                    this.guiManager.showNotification(ex.getMessage());
                    return null;
                });
    }

    private void onRegister(String username, String password) {
        var registerBean = new RegisterBean(username, password);

        authenticationBoundary.registerAsync(registerBean)
                .thenRun(() -> {
                    // Registration successful, proceed to the next screen
                    this.guiManager.showNotification("Registration successful!");
                    Navigator.getInstance().startHomeController();
                })
                .exceptionally(ex -> {
                    this.guiManager.showNotification(ex.getMessage());
                    return null;
                });
    }

    @Override
    public void start(GuiManager guiManager) {
        this.guiManager = guiManager;
        this.guiManager.setView(view);
    }
}
