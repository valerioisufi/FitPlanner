package com.example.fitplannerclient.ui.gui1.view;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class BaseView extends StackPane {

    public void showNotification(String message) {
        Label notification = new Label(message);
        notification.getStyleClass().add("notification-toast"); // Stile CSS

        notification.setWrapText(true);
        notification.setMaxWidth(300);

        StackPane.setAlignment(notification, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(notification, new Insets(0, 0, 20, 0));

        Platform.runLater(() -> {
            this.getChildren().add(notification);

            fadeAnimation(notification);
        });
    }

    private void fadeAnimation(Label notification) {
        PauseTransition delay = new PauseTransition(Duration.seconds(3));

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), notification);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> this.getChildren().remove(notification));

        delay.setOnFinished(e -> fadeOut.play());
        delay.play();
    }

}
