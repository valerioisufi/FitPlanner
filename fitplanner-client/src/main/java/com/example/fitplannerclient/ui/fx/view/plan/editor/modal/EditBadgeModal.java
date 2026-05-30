package com.example.fitplannerclient.ui.fx.view.plan.editor.modal;

import com.example.fitplannerclient.ui.fx.components.Icon;
import com.example.fitplannerclient.ui.fx.view.plan.editor.components.BadgeComponent;
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
import java.util.function.BiConsumer;

public class EditBadgeModal extends VBox {
    private final Label nameLabel;
    private final TextField valueField;
    private final Label titleLabel;

    // Callback that provides the updated Name and Value
    private BiConsumer<String, String> onSaveAction;
    private Runnable onCloseAction;

    public EditBadgeModal() {
        this.getStyleClass().add("card");
        this.setPadding(new Insets(32));
        this.setSpacing(24);
        this.setMaxWidth(400);
        this.setMaxHeight(Region.USE_PREF_SIZE);

        // --- HEADER ---
        HBox header = new HBox();
        header.setAlignment(Pos.TOP_LEFT);

        VBox titleBox = new VBox(4);
        titleLabel = new Label("Modifica Proprietà");
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

        // --- FORM FIELDS ---
        nameLabel = new Label();
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #424242; -fx-padding: 8 12; -fx-background-color: #F5F5F5; -fx-background-radius: 6;");

        VBox nameFieldBox = new VBox(8);
        Label nameLabelDesc = new Label("Tipo *");
        nameLabelDesc.getStyleClass().add("label-field");
        nameFieldBox.getChildren().addAll(nameLabelDesc, nameLabel);

        valueField = new TextField();
        valueField.setMaxWidth(Double.MAX_VALUE);
        valueField.setPromptText("Valore (es. 10, 90s, ecc)...");
        valueField.getStyleClass().add("text-field");

        VBox valueFieldBox = new VBox(8);
        Label valueLabelDesc = new Label("Valore *");
        valueLabelDesc.getStyleClass().add("label-field");
        valueFieldBox.getChildren().addAll(valueLabelDesc, valueField);

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
            if (onSaveAction != null && !valueField.getText().trim().isEmpty()) {
                onSaveAction.accept(nameLabel.getText(), valueField.getText().trim());
            }
        });

        footer.getChildren().addAll(cancelBtn, saveBtn);

        this.getChildren().addAll(header, nameFieldBox, valueFieldBox, footer);
    }

    public void setInitialData(BadgeComponent.BadgeType type, String name, String value) {
        nameLabel.setText(name);
        valueField.setText(value);
    }

    public void setOnSaveAction(BiConsumer<String, String> onSaveAction) {
        this.onSaveAction = onSaveAction;
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }
}
