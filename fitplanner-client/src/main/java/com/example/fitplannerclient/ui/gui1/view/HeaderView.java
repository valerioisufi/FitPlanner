package com.example.fitplannerclient.ui.gui1.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HeaderView extends HBox {
    public record MenuConfig(String title, String icon) {}

    // 1. Aggiungiamo una lista per conservare i riferimenti ai pulsanti
    private final List<Button> menuButtons = new ArrayList<>();

    public HeaderView(List<MenuConfig> menuItems, int activeBtnIndex) {
        this.getStyleClass().add("header");
        this.setPrefHeight(60);
        this.setAlignment(Pos.CENTER_LEFT);

        // titleContainer
        HBox titleContainer = new HBox(10);
        titleContainer.setAlignment(Pos.CENTER_LEFT);
        ImageView imageLogo = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app_icon.png"))));
        imageLogo.setPreserveRatio(true);

        Label title = new Label("FitPlanner");
        title.getStyleClass().add("h2");
        HBox.setMargin(titleContainer, new Insets(0, 0, 0, 20));

        titleContainer.getChildren().addAll(imageLogo, title);
        imageLogo.fitHeightProperty().bind(title.heightProperty().multiply(1.5));

        // spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // buttonsContainer
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
        this.getChildren().addAll(titleContainer, spacer, buttonsContainer);
    }

    // Metodo fondamentale per il Controller
    public List<Button> getMenuButtons() {
        return menuButtons;
    }
}
