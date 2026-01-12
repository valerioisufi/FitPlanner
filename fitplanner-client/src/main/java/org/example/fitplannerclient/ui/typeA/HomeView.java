package org.example.fitplannerclient.ui.typeA;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.example.fitplannerclient.ui.GraphicController;

import java.util.List;

public class HomeView extends VBox {
    private Button primaryButton;

    public HomeView(Node header) {
        this.setSpacing(16);
        this.getStyleClass().add("root");

        if (header != null) this.getChildren().add(header);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Text welcomeText = new Text("Benvenuto in FitPlanner!");
        welcomeText.getStyleClass().add("h1");
        Text subWelcomeText = new Text("Oggi è il momento perfetto per superare i tuoi limiti");
        subWelcomeText.getStyleClass().add("h2");

        content.getChildren().addAll(welcomeText, subWelcomeText);

        this.getChildren().add(content);

    }




}
