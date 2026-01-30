package com.example.fitplannerclient.ui.gui1;

import com.example.fitplannerclient.ui.gui1.view.BaseView;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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
        this.scene = new Scene(this);
        stage.setScene(scene);

        String themeCss = Objects.requireNonNull(getClass().getResource("/style/theme1.css")).toExternalForm();
        String iconsCss = Objects.requireNonNull(getClass().getResource("/style/icons.css")).toExternalForm();
        this.getStylesheets().addAll(themeCss, iconsCss);

        this.getStyleClass().add("root"); // Background globale

        // Area Contenuto (dove andranno HomeView, LoginView, ecc.)
        contentArea = new StackPane();
        contentArea.setAlignment(Pos.CENTER);

        // Area Notifiche (Overlay)
        notificationArea = new VBox(10);
        notificationArea.setAlignment(Pos.BOTTOM_RIGHT);
        notificationArea.setPadding(new Insets(20));
        notificationArea.setPickOnBounds(false);

        // Aggiungi nell'ordine: contenuto sotto, notifiche sopra
        this.getChildren().addAll(contentArea, notificationArea);
    }

    /**
     * Sostituisce la vista centrale
     */
    public void setView(BaseView view) {
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