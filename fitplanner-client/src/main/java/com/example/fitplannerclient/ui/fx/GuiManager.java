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

public class GuiManager extends StackPane {
    private final Stage stage;
    private final Scene scene;

    private final StackPane contentArea;
    private final VBox notificationArea;

    public GuiManager(Stage stage) {
        this.stage = stage;
        this.scene = new Scene(this, 1200, 800);
        stage.setScene(scene);

        String themeCss = Objects.requireNonNull(getClass().getResource("/style/theme.css")).toExternalForm();
        this.getStylesheets().addAll(themeCss);

        // area contenuto (HomeView, LoginView, ecc.)
        contentArea = new StackPane();
        contentArea.setAlignment(Pos.CENTER);

        // area notifiche
        notificationArea = new VBox(10);
        notificationArea.setAlignment(Pos.BOTTOM_RIGHT);
        notificationArea.setPadding(new Insets(20));
        notificationArea.setPickOnBounds(false);

        this.getChildren().addAll(contentArea, notificationArea);
    }

    /**
     * Sostituisce la vista centrale
     */
    public void setView(Pane view) {
        Platform.runLater(() -> {
            contentArea.getChildren().setAll(view);
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
