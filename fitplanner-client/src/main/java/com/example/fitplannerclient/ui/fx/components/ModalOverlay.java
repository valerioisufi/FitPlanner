package com.example.fitplannerclient.ui.fx.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;

public class ModalOverlay extends ScrollPane {

    private final StackPane contentWrapper;

    public ModalOverlay(Node modalContent) {
        this.contentWrapper = new StackPane();
        if (modalContent != null) {
            this.contentWrapper.getChildren().add(modalContent);
        }
        this.contentWrapper.setAlignment(Pos.CENTER);
        this.contentWrapper.setPadding(new Insets(24, 0, 24, 0));
        this.contentWrapper.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");

        this.setContent(contentWrapper);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        this.setVisible(false);
    }

    public void show() {
        this.setVisible(true);
    }

    public void hide() {
        this.setVisible(false);
    }

    public void setModalContent(Node modalContent) {
        this.contentWrapper.getChildren().setAll(modalContent);
    }
}
