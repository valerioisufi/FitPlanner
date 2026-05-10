package com.example.fitplannerclient;

import com.example.fitplannerclient.ui.fx.PlanNodeComponent;
import com.example.fitplannerclient.ui.fx.PlanViewer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class UIDebugger extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("debug window");

        PlanViewer planViewer = new PlanViewer();

        ScrollPane root = new ScrollPane();
        root.setContent(planViewer);
        root.setFitToWidth(true);
        root.setPannable(true);
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root, 1200, 800);

        String themeCss = Objects.requireNonNull(getClass().getResource("/style/theme.css")).toExternalForm();
        scene.getStylesheets().addAll(themeCss);

        stage.setScene(scene);
        stage.show();

//        Stage myPopup = new Stage();
//        myPopup.initStyle(StageStyle.TRANSPARENT);
//        myPopup.setAlwaysOnTop(true);
//        myPopup.setScene(new Scene(new PlanNodeComponent(), 400, 300));
//        myPopup.show();
    }
}
