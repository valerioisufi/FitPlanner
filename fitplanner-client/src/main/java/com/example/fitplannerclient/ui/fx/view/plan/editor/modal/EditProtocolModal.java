package com.example.fitplannerclient.ui.fx.view.plan.editor.modal;

import com.example.fitplannerclient.ui.fx.components.FormField;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EditProtocolModal extends VBox {

    private final VBox formContainer = new VBox(15);
    private final Label titleLabel = new Label("Imposta Parametri Protocollo");

    private Runnable onCloseAction;
    private Consumer<Map<String, String>> onSaveAction;

    private Map<String, String> currentParameters;
    private final Map<String, TextField> inputFields = new HashMap<>();

    public EditProtocolModal() {
        this.getStyleClass().add("card");
        this.setPadding(new Insets(32));
        this.setSpacing(24);
        this.setMaxWidth(450);
        this.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

        // --- HEADER ---
        HBox header = new HBox();
        header.setAlignment(Pos.TOP_LEFT);

        VBox titleBox = new VBox(4);
        titleLabel.getStyleClass().add("heading-h2");
        titleBox.getChildren().add(titleLabel);

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button closeBtn = new Button();
        closeBtn.getStyleClass().add("button-header");
        closeBtn.setGraphic(new com.example.fitplannerclient.ui.fx.components.Icon("x-icon", java.util.List.of("button-header-icon")));
        closeBtn.setOnAction(e -> {
            if (onCloseAction != null) onCloseAction.run();
        });

        header.getChildren().addAll(titleBox, spacer, closeBtn);

        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 0, 0, 0));
        footer.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 1 0 0 0;");
        
        Button cancelBtn = new Button("Annulla");
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setOnAction(e -> { if (onCloseAction != null) onCloseAction.run(); });

        Button saveBtn = new Button("Salva");
        saveBtn.setDefaultButton(true);
        saveBtn.getStyleClass().add("button-primary");
        saveBtn.setOnAction(e -> {
            if (onSaveAction != null && currentParameters != null) {
                Map<String, String> result = new HashMap<>();
                for (Map.Entry<String, TextField> entry : inputFields.entrySet()) {
                    result.put(entry.getKey(), entry.getValue().getText());
                }
                onSaveAction.accept(result);
            }
        });

        footer.getChildren().addAll(cancelBtn, saveBtn);

        this.getChildren().addAll(header, formContainer, footer);
    }

    public void setInitialData(String protocolName, Map<String, String> parameters) {
        titleLabel.setText("Parametri: " + protocolName);
        this.currentParameters = parameters;
        formContainer.getChildren().clear();
        inputFields.clear();

        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                TextField input = new TextField(entry.getValue());
                FormField field = new FormField(entry.getKey(), "Inserisci " + entry.getKey(), input);
                inputFields.put(entry.getKey(), input);
                formContainer.getChildren().add(field);
            }
        }
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }

    public void setOnSaveAction(Consumer<Map<String, String>> onSaveAction) {
        this.onSaveAction = onSaveAction;
    }
}
