package com.example.fitplannerclient.ui.fx.view.plan.editor.modal;

import com.example.fitplannerclient.ui.fx.components.Icon;
import com.example.fitplannerclient.ui.fx.view.plan.editor.components.BadgeComponent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class EditBadgeModal extends VBox {

    private final Label nameLabel;
    private final TextField valueField;
    private final Label valueLabelDesc;
    private final Label hintLabel;
    
    private final ToggleGroup toggleGroup;
    private final RadioButton fixedValueRadio;
    private final RadioButton variableRadio;
    private final HBox toggleBox;
    
    private final ComboBox<String> variableComboBox;
    
    private final VBox inputContainer;

    private Consumer<String> onSaveAction;
    private Runnable onCloseAction;

    public EditBadgeModal() {
        this.getStyleClass().add("card");
        this.setPadding(new Insets(32));
        this.setSpacing(24);
        this.setMaxWidth(400);
        this.setMaxHeight(Region.USE_PREF_SIZE);

        // --- HEADER ---
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label titleLabel = new Label("Modifica Valore");
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

        // --- TOGGLE FISSO / VARIABILE ---
        toggleGroup = new ToggleGroup();
        fixedValueRadio = new RadioButton("Valore Fisso");
        fixedValueRadio.setStyle("-fx-text-fill: #0F172A;");
        fixedValueRadio.setToggleGroup(toggleGroup);
        fixedValueRadio.setSelected(true);
        variableRadio = new RadioButton("Variabile");
        variableRadio.setStyle("-fx-text-fill: #0F172A;");
        variableRadio.setToggleGroup(toggleGroup);

        toggleBox = new HBox(16, fixedValueRadio, variableRadio);
        toggleBox.setAlignment(Pos.CENTER_LEFT);

        // --- INPUT FIELDS ---
        valueField = new TextField();
        valueField.setMaxWidth(Double.MAX_VALUE);
        valueField.getStyleClass().add("text-field");
        
        hintLabel = new Label();
        hintLabel.setStyle("-fx-text-fill: #9E9E9E; -fx-font-size: 11px;");
        hintLabel.setWrapText(true);
        hintLabel.setVisible(false);
        hintLabel.setManaged(false);

        variableComboBox = new ComboBox<>();
        variableComboBox.setMaxWidth(Double.MAX_VALUE);
        variableComboBox.setVisible(false);
        variableComboBox.setManaged(false);
        
        inputContainer = new VBox(8);
        valueLabelDesc = new Label("Valore *");
        valueLabelDesc.getStyleClass().add("label-field");
        inputContainer.getChildren().addAll(valueLabelDesc, valueField, hintLabel, variableComboBox);

        setupToggleListener();
        setupValidationListener();

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
        saveBtn.setDefaultButton(true);
        saveBtn.getStyleClass().add("button-primary");
        saveBtn.setOnAction(e -> {
            if (onSaveAction != null) {
                String resultValue = "";
                if (!toggleBox.isVisible() || fixedValueRadio.isSelected()) {
                    resultValue = valueField.getText().trim();
                } else {
                    resultValue = variableComboBox.getValue() != null ? variableComboBox.getValue() : "";
                }
                
                if (!resultValue.isEmpty()) {
                    onSaveAction.accept(resultValue);
                }
            }
        });

        footer.getChildren().addAll(cancelBtn, saveBtn);

        this.getChildren().addAll(header, nameFieldBox, toggleBox, inputContainer, footer);
    }

    private void setupToggleListener() {
        toggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == fixedValueRadio) {
                valueField.setVisible(true);
                valueField.setManaged(true);
                variableComboBox.setVisible(false);
                variableComboBox.setManaged(false);
            } else if (newVal == variableRadio) {
                valueField.setVisible(false);
                valueField.setManaged(false);
                variableComboBox.setVisible(true);
                variableComboBox.setManaged(true);
            }
        });
    }

    private void setupValidationListener() {
        valueField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            // Allow decimals for weight/distance, but integer for rounds/time.
            // A simple regex that allows numbers and optional decimal point.
            boolean isTut = nameLabel.getText() != null && nameLabel.getText().equalsIgnoreCase("TUT");
            if (isTut) {
                if (!newVal.matches("[\\d/\\-]*") && toggleBox.isVisible() && fixedValueRadio.isSelected()) {
                    valueField.setText(oldVal);
                }
            } else if (!newVal.matches("\\d*\\.?\\d*") && toggleBox.isVisible() && fixedValueRadio.isSelected()) {
                valueField.setText(oldVal);
            }
        });
    }

    public void setInitialData(BadgeComponent.BadgeType type, String name, String value, List<String> availableVariables) {
        nameLabel.setText(name);
        
        variableComboBox.getItems().clear();
        if (availableVariables != null) {
            variableComboBox.getItems().addAll(availableVariables);
        }
        
        if (name.equalsIgnoreCase("PROGRESSION")) {
            toggleBox.setVisible(false);
            toggleBox.setManaged(false);
            
            valueLabelDesc.setText("Regole di Progressione *");
            valueField.setText(value);
            valueField.setPromptText("Es. WEIGHT: 50, 52.5, 55; REPS: 10, 8, 6");
            
            hintLabel.setText("Formato: CHIAVE1: val1, val2; CHIAVE2: val1, val2");
            hintLabel.setVisible(true);
            hintLabel.setManaged(true);
            
            fixedValueRadio.setSelected(true);
            
            // Remove text formatter restriction for progression
            valueField.textProperty().set(value);
        } else {
            toggleBox.setVisible(true);
            toggleBox.setManaged(true);
            hintLabel.setVisible(false);
            hintLabel.setManaged(false);
            
            // Setup label based on unit
            String unit = getUnitForType(name);
            valueLabelDesc.setText(unit.isEmpty() ? "Valore *" : "Valore (" + unit + ") *");
            valueField.setPromptText(unit.isEmpty() ? "Inserisci valore..." : "Inserisci solo il numero...");
            
            if (value != null && value.startsWith("${") && value.endsWith("}")) {
                variableRadio.setSelected(true);
                variableComboBox.setValue(value);
                valueField.setText("");
            } else {
                fixedValueRadio.setSelected(true);
                valueField.setText(value);
                variableComboBox.setValue(null);
            }
            
            if (name.equalsIgnoreCase("TUT")) {
                valueField.setPromptText("Es. 3/1/1/0");
                hintLabel.setText("Formato: eccentrica/isometria in allungamento/concentrica/isometria in accorciamento");
                hintLabel.setVisible(true);
                hintLabel.setManaged(true);
            }
        }
    }

    private String getUnitForType(String type) {
        return switch (type.toUpperCase()) {
            case "REST", "TIME_LIMIT", "INTERVAL", "TEMPO" -> "secondi";
            case "LOOP", "SETS" -> "round";
            case "REPS" -> "ripetizioni";
            case "WEIGHT" -> "kg";
            case "DISTANCE" -> "km";
            default -> "";
        };
    }

    public void setOnSaveAction(Consumer<String> onSaveAction) {
        this.onSaveAction = onSaveAction;
    }

    public void setOnCloseAction(Runnable onCloseAction) {
        this.onCloseAction = onCloseAction;
    }
}
