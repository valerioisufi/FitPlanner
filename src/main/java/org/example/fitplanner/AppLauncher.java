package org.example.fitplanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.example.fitplanner.view.HomeView;
import org.example.fitplanner.view.RegistrationView;

import java.util.Objects;

public class AppLauncher extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        stage.setTitle("FitPlanner");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/appIcon.png"))));

        var scene = new Scene(new HomeView());

        String css = Objects.requireNonNull(getClass().getResource("/style/theme1.css")).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setScene(scene);
        stage.show();

    }
}