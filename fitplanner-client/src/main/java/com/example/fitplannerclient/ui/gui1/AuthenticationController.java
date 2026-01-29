package com.example.fitplannerclient.ui.gui1;

import com.example.fitplannerclient.service.AuthenticationBoundary;
import com.example.fitplannerclient.ui.GraphicController;
import com.example.fitplannerclient.ui.gui1.view.RegistrationView;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class AuthenticationController implements GraphicController {
    RegistrationView view;

    public AuthenticationController(AuthenticationBoundary authenticationBoundary) {
        view = new RegistrationView();

        String themeCss = Objects.requireNonNull(getClass().getResource("/style/style.css")).toExternalForm();
        String iconsCss = Objects.requireNonNull(getClass().getResource("/style/icons.css")).toExternalForm();
        view.getStylesheets().addAll(themeCss, iconsCss);
    }

    @Override
    public void start(Stage stage) {
        stage.setScene(new Scene(view));
        Scene scene = stage.getScene();
        scene.setRoot(view);
        stage.show();
    }
}
