package com.example.fitplannerclient.ui.gui1.view;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Objects;

public abstract class BaseView {

    private final StackPane rootStackPane;
    private final BorderPane mainLayout;

    protected BaseView() {
        this.rootStackPane = new StackPane();
        String themeCss = Objects.requireNonNull(getClass().getResource("/style/theme1.css")).toExternalForm();
        String iconsCss = Objects.requireNonNull(getClass().getResource("/style/icons.css")).toExternalForm();
        rootStackPane.getStylesheets().addAll(themeCss, iconsCss);

        this.rootStackPane.getStyleClass().add("root");

        this.mainLayout = new BorderPane();
        this.rootStackPane.getChildren().add(mainLayout);
    }

    public Node getRootNode() {
        return rootStackPane;
    }

    public void setHeader(Node headerView) {
        mainLayout.setTop(headerView);
    }

    public void setMainContent(Node content) {
        mainLayout.setCenter(content);
    }

    public void showNotification(String message) {
        Label notification = new Label(message);
        notification.getStyleClass().add("notification-toast"); // Stile CSS

        notification.setWrapText(true);

        StackPane.setAlignment(notification, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(notification, new Insets(0, 20, 20, 0));

        Platform.runLater(() -> {
            this.rootStackPane.getChildren().add(notification);

            fadeAnimation(notification);
        });
    }

    private void fadeAnimation(Label notification) {
        PauseTransition delay = new PauseTransition(Duration.seconds(3));

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), notification);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> this.rootStackPane.getChildren().remove(notification));

        delay.setOnFinished(e -> fadeOut.play());
        delay.play();
    }

}
