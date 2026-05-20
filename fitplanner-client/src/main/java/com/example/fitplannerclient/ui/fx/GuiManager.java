package com.example.fitplannerclient.ui.fx;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;

public class GuiManager {
    private final Stage stage;
    private final Scene scene;

    private final StackPane rootPane;
    private final StackPane contentArea;
    private final StackPane overlayArea;
    private final VBox notificationArea;

    public GuiManager(Stage stage) {
        this.stage = stage;

        this.rootPane = new StackPane();
        this.scene = new Scene(rootPane, 1200, 800);
        this.stage.setScene(scene);

        String themeCss = Objects.requireNonNull(getClass().getResource("/style/theme.css")).toExternalForm();
        this.scene.getStylesheets().add(themeCss);

        this.contentArea = new StackPane();
        this.contentArea.setAlignment(Pos.CENTER);

        this.notificationArea = new VBox(10);
        this.notificationArea.setAlignment(Pos.BOTTOM_RIGHT);
        this.notificationArea.setPadding(new Insets(20));
        this.notificationArea.setPickOnBounds(false);

        this.overlayArea = new StackPane();
        this.overlayArea.setVisible(false);
        this.overlayArea.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        this.rootPane.getChildren().addAll(contentArea, overlayArea, notificationArea);
    }

    /**
     * Sostituisce la vista centrale
     */
    public void setView(Pane view) {
        Platform.runLater(() -> contentArea.getChildren().setAll(view));
    }

    public void showOverlay(Pane view) {
        Platform.runLater(() -> {
            overlayArea.getChildren().setAll(view);
            overlayArea.setVisible(true);
        });
    }

    public void hideOverlay() {
        Platform.runLater(() -> {
            overlayArea.getChildren().clear();
            overlayArea.setVisible(false);
        });
    }

    /**
     * Mostra una notifica
     */
    public void showNotification(String message) {
        Label notification = new Label(message);
        notification.getStyleClass().add("notification-toast");
        notification.setWrapText(true);

        Platform.runLater(() -> {
            notificationArea.getChildren().add(notification);
            animateNotification(notification);
        });
    }

    private void animateNotification(Label notification) {
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), notification);

        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> notificationArea.getChildren().remove(notification));

        delay.setOnFinished(e -> fadeOut.play());
        delay.play();
    }
}
