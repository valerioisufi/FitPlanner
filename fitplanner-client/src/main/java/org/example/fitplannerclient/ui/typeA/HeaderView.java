package org.example.fitplannerclient.ui.typeA;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

public class HeaderView extends HBox {
    public record MenuConfig(String title, String icon) {}

    // 1. Aggiungiamo una lista per conservare i riferimenti ai pulsanti
    private final List<Button> menuButtons = new ArrayList<>();

    HeaderView(List<MenuConfig> menuItems, int activeBtnIndex) {
        this.getStyleClass().add("header");
        this.setPrefHeight(60);
        this.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("FitPlanner");
        title.getStyleClass().add("h2");
        // Aggiungiamo un margine sinistro al titolo se serve, o usiamo il padding dell'HBox
        HBox.setMargin(title, new Insets(0, 0, 0, 20));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonsContainer = new HBox(15);
        buttonsContainer.setAlignment(Pos.CENTER_RIGHT);
        HBox.setMargin(buttonsContainer, new Insets(0, 20, 0, 0));

        int i = 0;
        for (MenuConfig item : menuItems) {
            Region icon = new Region();
            icon.getStyleClass().add(item.icon);
            icon.setPrefSize(20, 20);

            Button btn = new Button(item.title);
            btn.setGraphic(icon);
            btn.setGraphicTextGap(10);
            btn.getStyleClass().add("button-header");

            if (i == activeBtnIndex) {
                btn.getStyleClass().add("button-header-active");
                icon.getStyleClass().add("button-header-icon-active");
            } else {
                icon.getStyleClass().add("button-header-icon");
            }

            // Salviamo il pulsante nella lista per il Controller!
            menuButtons.add(btn);
            buttonsContainer.getChildren().add(btn);

            i++;
        }

        // Aggiungiamo i tre blocchi principali all'HBox radice
        this.getChildren().addAll(title, spacer, buttonsContainer);
    }

    // Metodo fondamentale per il Controller
    public List<Button> getMenuButtons() {
        return menuButtons;
    }
}
