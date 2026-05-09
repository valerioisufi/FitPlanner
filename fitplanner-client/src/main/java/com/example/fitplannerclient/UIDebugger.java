package com.example.fitplannerclient;

import com.example.fitplannerclient.ui.fx.PlanNodeComponent;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Objects;

public class UIDebugger extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("debug window");

        VBox root = new VBox();
        root.getChildren().add(new PlanNodeComponent());
        Scene scene = new Scene(root, 1200, 800);

        String themeCss = Objects.requireNonNull(getClass().getResource("/style/theme1.css")).toExternalForm();
        String iconsCss = Objects.requireNonNull(getClass().getResource("/style/icons.css")).toExternalForm();
        scene.getStylesheets().addAll(themeCss, iconsCss);

        stage.setScene(scene);
        stage.show();

//        Stage myPopup = new Stage();
////        myPopup.initStyle(StageStyle.TRANSPARENT);
//        myPopup.setAlwaysOnTop(true);
//        myPopup.setScene(new Scene(new PlanNodeComponent(), 400, 300));
//        myPopup.show();
    }
}
