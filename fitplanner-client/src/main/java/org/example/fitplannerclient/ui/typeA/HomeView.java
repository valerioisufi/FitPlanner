package org.example.fitplannerclient.ui.typeA;

import javafx.geometry.Pos;
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

    public HomeView() {
        this.setSpacing(16);
        this.getStyleClass().add("root");

        Text welcomeText = new Text("Benvenuto in FitPlanner!");
        welcomeText.getStyleClass().add("h1");
        Text subWelcomeText = new Text("Oggi è il momento perfetto per superare i tuoi limiti");
        subWelcomeText.getStyleClass().add("h2");

        HBox header = createHeader(0);
        this.getChildren().add(header);
        this.getChildren().addAll(welcomeText, subWelcomeText);

    }

    record MenuConfig(String title, String icon) {}

    private HBox createHeader(int activeBtnIndex) {
        Label title = new Label("FitPlanner");
        title.getStyleClass().add("h2");

        List<MenuConfig> menuItems = List.of(
                new MenuConfig("Home", "home-icon"),
                new MenuConfig("Piano", "plan-icon"),
                new MenuConfig("Progressi", "progress-icon")
        );

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setSpacing(40);
        header.setPrefHeight(60);
        header.getChildren().add(title);
        header.getStyleClass().add("header");

        int i = 0;
        for (MenuConfig item : menuItems) {
            Region icon = new Region();
            icon.getStyleClass().add(item.icon);

            icon.setPrefSize(20, 20);

            Button btn = new Button(item.title);
            btn.setGraphic(icon);
            btn.setGraphicTextGap(10);

            btn.getStyleClass().add("button-header");
            if (i++ == activeBtnIndex) btn.getStyleClass().add("button-header-active");
            header.getChildren().add(btn);
        }

        return header;
    }


}
