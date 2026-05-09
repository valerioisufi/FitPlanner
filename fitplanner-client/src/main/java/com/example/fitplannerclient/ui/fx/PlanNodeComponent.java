package com.example.fitplannerclient.ui.fx;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class PlanNodeComponent extends VBox {
    Text name;

    public PlanNodeComponent() {
        this.setStyle("-fx-spacing: 16; -fx-padding: 16;");
        this.setPrefSize(200, 100);

        this.name = new Text("Barbell Squat");
        name.getStyleClass().add("heading-h1");

        this.getChildren().add(name);
    }

}
