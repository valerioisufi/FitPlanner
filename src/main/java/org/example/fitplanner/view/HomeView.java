package org.example.fitplanner.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HomeView extends VBox {
    private Button primaryButton;

    public HomeView() {
        this.setSpacing(16);
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("root");

        primaryButton = new Button("Get Started");
        primaryButton.setPrefSize(200, 50);
        primaryButton.getStyleClass().add("button-primary");

        this.getChildren().add(new Label("Welcome to FitPlanner!"));
        this.getChildren().add(primaryButton);

    }

}
