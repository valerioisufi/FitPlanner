package com.example.fitplannerclient.ui.gui1;

import com.example.fitplannerclient.Navigator;
import com.example.fitplannerclient.service.AuthenticationBoundary;
import com.example.fitplannerclient.ui.GraphicController;
import com.example.fitplannerclient.ui.gui1.view.AuthenticationView;
import com.example.fitplannercommon.LoginBean;
import com.example.fitplannercommon.RegisterBean;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class AuthenticationController implements GraphicController {
    AuthenticationBoundary authenticationBoundary;
    AuthenticationView view;

    public AuthenticationController(AuthenticationBoundary authenticationBoundary) {
        this.authenticationBoundary = authenticationBoundary;

        view = new AuthenticationView();
        String themeCss = Objects.requireNonNull(getClass().getResource("/style/theme1.css")).toExternalForm();
        String iconsCss = Objects.requireNonNull(getClass().getResource("/style/icons.css")).toExternalForm();
        view.getStylesheets().addAll(themeCss, iconsCss);

        view.setLoginBtnAction(() -> this.onLogin(this.view.getUsername(), this.view.getPassword()));
        view.setRegistrationBtnAction(() -> this.onRegister(this.view.getUsername(), this.view.getPassword()));
    }

    private void onLogin(String username, String password) {
        var loginBean = new LoginBean(username, password);

        authenticationBoundary.loginAsync(loginBean)
                .thenRun(() -> {
                    // Login successful, proceed to the next screen
                    System.out.println("Login successful!");
                    Navigator.getInstance().startHomeController();
                })
                .exceptionally(ex -> {
                    this.view.showNotification(ex.getMessage());
                    return null;
                });
    }

    private void onRegister(String username, String password) {
        var registerBean = new RegisterBean(username, password);

        authenticationBoundary.registerAsync(registerBean)
                .thenRun(() -> {
                    // Registration successful, proceed to the next screen
                    System.out.println("Registration successful!");
                    Navigator.getInstance().startHomeController();
                })
                .exceptionally(ex -> {
                    this.view.showNotification(ex.getMessage());
                    return null;
                });
    }

    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(view));
        stage.show();
    }
}
