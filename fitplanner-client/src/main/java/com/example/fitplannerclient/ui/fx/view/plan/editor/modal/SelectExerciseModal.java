package com.example.fitplannerclient.ui.fx.view.plan.editor.modal;

import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class SelectExerciseModal extends VBox {

    private final TextField searchField = new TextField();
    private final VBox exerciseListContainer = new VBox(10);
    private Runnable onCloseAction;
    private Consumer<ExerciseDescriptionBean> onSaveAction;

    private List<ExerciseDescriptionBean> allExercises;

    public SelectExerciseModal() {
        this.setSpacing(20);
        this.setPadding(new Insets(24));
        this.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        this.setMaxWidth(400);

        Label titleLabel = new Label("Seleziona Esercizio");
        titleLabel.getStyleClass().add("heading-h2");

        searchField.setPromptText("Cerca per nome o gruppo muscolare...");
        searchField.getStyleClass().add("text-field");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterExercises(newVal));

        ScrollPane scrollPane = new ScrollPane(exerciseListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);

        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button cancelBtn = new Button("Annulla");
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setOnAction(e -> { if (onCloseAction != null) onCloseAction.run(); });

        footer.getChildren().add(cancelBtn);

        this.getChildren().addAll(titleLabel, searchField, scrollPane, footer);
    }

    public void setExercises(List<ExerciseDescriptionBean> exercises) {
        this.allExercises = exercises;
        filterExercises(searchField.getText());
    }

    private void filterExercises(String query) {
        exerciseListContainer.getChildren().clear();
        if (allExercises == null) return;

        String q = query != null ? query.toLowerCase() : "";

        for (ExerciseDescriptionBean ex : allExercises) {
            boolean matchesName = ex.getName().toLowerCase().contains(q);
            boolean matchesMuscle = ex.getMuscleGroups() != null && ex.getMuscleGroups().stream()
                    .anyMatch(m -> m.toLowerCase().contains(q));

            if (matchesName || matchesMuscle) {
                exerciseListContainer.getChildren().add(createExerciseItem(ex));
            }
        }
    }

    private VBox createExerciseItem(ExerciseDescriptionBean ex) {
        VBox item = new VBox(5);
        item.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-cursor: hand;");

        Label name = new Label(ex.getName());
        name.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 13px; -fx-text-fill: -fx-color-text-body;");

        FlowPane tags = new FlowPane(4, 4);
        if (ex.getMuscleGroups() != null) {
            for (String m : ex.getMuscleGroups()) {
                Label tag = new Label(m);
                tag.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #475569; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 9px;");
                tags.getChildren().add(tag);
            }
        }

        item.getChildren().addAll(name, tags);

        item.setOnMouseClicked(e -> {
            if (onSaveAction != null) {
                onSaveAction.accept(ex);
            }
        });

        // Add hover effect
        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: #E2E8F0; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #CBD5E1; -fx-border-radius: 8; -fx-cursor: hand;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-cursor: hand;"));

        return item;
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    public void setOnSaveAction(Consumer<ExerciseDescriptionBean> onSaveAction) {
        this.onSaveAction = onSaveAction;
    }
}
