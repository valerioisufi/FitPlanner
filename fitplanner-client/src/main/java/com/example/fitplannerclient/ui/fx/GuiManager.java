package com.example.fitplannerclient.ui.fx;

import com.example.fitplannerclient.exception.NotAuthenticatedException;
import com.example.fitplannerclient.exception.RequestException;
import com.example.fitplannerclient.ui.fx.components.ModalOverlay;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
    private final StackPane modalArea;
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

        this.modalArea = new StackPane();
        this.modalArea.setVisible(false);

        this.overlayArea = new StackPane();
        this.overlayArea.setVisible(false);
        this.overlayArea.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        this.rootPane.getChildren().addAll(contentArea, modalArea, overlayArea, notificationArea);
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

    public void showModal(Node content) {
        Platform.runLater(() -> {
            ModalOverlay modal = new ModalOverlay(content);
            modal.show();

            modalArea.getChildren().add(modal);
            modalArea.setVisible(true);
        });
    }

    public void hideModal() {
        Platform.runLater(() -> {
            if (!modalArea.getChildren().isEmpty()) {
                modalArea.getChildren().removeLast();
            }
            
            if (modalArea.getChildren().isEmpty()) {
                modalArea.setVisible(false);
            }
        });
    }

    public void clearModals() {
        Platform.runLater(() -> {
            modalArea.getChildren().clear();
            modalArea.setVisible(false);
        });
    }

    public enum NotificationType {
        SUCCESS, ERROR, INFO
    }

    /**
     * Mostra una notifica
     */
    public void showNotification(NotificationType type, String message) {
        Label notification = new Label(message);
        notification.getStyleClass().add("notification-toast");
        
        switch (type) {
            case ERROR:
                notification.getStyleClass().add("notification-toast-error");
                break;
            case SUCCESS:
                notification.getStyleClass().add("notification-toast-success");
                break;
            default:
                notification.getStyleClass().add("notification-toast-info");
                break;
        }
        notification.setWrapText(true);

        Platform.runLater(() -> {
            notificationArea.getChildren().add(notification);
            animateNotification(notification);
        });
    }

    /**
     * Mostra una notifica di errore estraendo in modo sicuro il messaggio dall'eccezione
     */
    public void showExceptionError(String prefixMessage, Throwable ex) {
        Throwable realException = ex;
        if (realException != null) {
            while (realException.getCause() != null) {
                if (realException instanceof RequestException ||
                    realException instanceof NotAuthenticatedException) {
                    break;
                }
                realException = realException.getCause();
            }
        }
        
        String errorMsg = (realException != null && realException.getMessage() != null && !realException.getMessage().isBlank()) 
                            ? realException.getMessage() 
                            : "Errore sconosciuto";
                            
        String finalMessage = (prefixMessage != null && !prefixMessage.isBlank()) 
                              ? prefixMessage + " " + errorMsg 
                              : errorMsg;
                              
        showNotification(NotificationType.ERROR, finalMessage);
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
