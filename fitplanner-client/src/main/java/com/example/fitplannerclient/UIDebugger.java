package com.example.fitplannerclient;

import com.example.fitplannerclient.ui.fx.PlanViewer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class UIDebugger extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("UI Debug Window");

        VBox root = new VBox();
        root.getStyleClass().addAll("card");
        PlanViewer planViewer = new PlanViewer();

        VBox.setVgrow(planViewer, Priority.ALWAYS);
        root.getChildren().add(planViewer);

        Scene scene = new Scene(root, 1200, 800);

        String themeCss = Objects.requireNonNull(getClass().getResource("/style/theme.css")).toExternalForm();
        scene.getStylesheets().addAll(themeCss);

        stage.setScene(scene);
        stage.show();
    }
}
