package com.example.fitplannerclient.ui.gui1;

import com.example.fitplannerclient.service.AuthenticationBoundary;
import com.example.fitplannerclient.ui.GraphicController;
import com.example.fitplannerclient.ui.gui1.view.AuthenticationView;
import com.example.fitplannerclient.ui.gui1.view.RegistrationView;
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
    }


    private void login(){

    }


    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(view));
        stage.show();
    }
}
