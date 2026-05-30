package com.example.fitplannerclient.ui.fx.view.plan.editor.modal;

import com.example.fitplannerclient.ui.fx.components.Icon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class EditNodeNameModal extends VBox {

    private final TextField nameField;
    private final Label titleLabel;

    private Consumer<String> onSaveAction;
    private Runnable onCloseAction;

    public EditNodeNameModal() {
        this.getStyleClass().add("card");
        this.setPadding(new Insets(32));
        this.setSpacing(24);
        this.setMaxWidth(400);
        this.setMaxHeight(Region.USE_PREF_SIZE);

        // --- HEADER ---
        HBox header = new HBox();
        header.setAlignment(Pos.TOP_LEFT);

        VBox titleBox = new VBox(4);
        titleLabel = new Label("Modifica Nome Nodo");
        titleLabel.getStyleClass().add("heading-h2");
        titleBox.getChildren().addAll(titleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button();
        closeBtn.getStyleClass().add("button-header");
        closeBtn.setGraphic(new Icon("x-icon", List.of("button-header-icon")));
        closeBtn.setOnAction(e -> {
            if (onCloseAction != null) onCloseAction.run();
        });

        header.getChildren().addAll(titleBox, spacer, closeBtn);

        // --- FORM FIELD ---
        nameField = new TextField();
        nameField.setMaxWidth(Double.MAX_VALUE);
        nameField.setPromptText("Inserisci un nuovo nome...");
        nameField.getStyleClass().add("text-field");

        VBox nameFieldBox = new VBox(8);
        Label nameLabelDesc = new Label("Nome *");
        nameLabelDesc.getStyleClass().add("label-field");
        nameFieldBox.getChildren().addAll(nameLabelDesc, nameField);

        // --- FOOTER ACTIONS ---
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 0, 0, 0));
        footer.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");

        Button cancelBtn = new Button("Annulla");
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setOnAction(e -> {
            if (onCloseAction != null) onCloseAction.run();
        });

        Button saveBtn = new Button("Salva");
        saveBtn.getStyleClass().add("button-primary");
        saveBtn.setOnAction(e -> {
            if (onSaveAction != null && !nameField.getText().trim().isEmpty()) {
                onSaveAction.accept(nameField.getText().trim());
            }
        });

        footer.getChildren().addAll(cancelBtn, saveBtn);

        this.getChildren().addAll(header, nameFieldBox, footer);
    }

    public void setInitialName(String name) {
        nameField.setText(name);
    }

    public void setOnSaveAction(Consumer<String> onSaveAction) {
        this.onSaveAction = onSaveAction;
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }
}
