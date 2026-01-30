package com.example.fitplannerclient;

import com.example.fitplannerclient.service.SessionManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Objects;

public class AppLauncher extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("FitPlanner");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app_icon.png"))));
        stage.setMinWidth(1200);
        stage.setMinHeight(800);

        SessionManager.getInstance().logout();

        Navigator navigator = Navigator.getInstance();
        navigator.setPrimaryStage(stage);
        navigator.startHomeController();

        stage.show();
    }

}