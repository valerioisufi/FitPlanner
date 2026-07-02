package com.example.fitplannerclient.ui.fx.components;

import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.VBox;

import java.util.function.UnaryOperator;

public class FormField extends VBox {

    private static final String INPUT_ERROR_CLASS = "input-error";

    private final Label titleLabel;
    private final TextInputControl inputField;
    private final Label errorLabel;

    // lambda that takes the input text and returns an error message (or null if valid)
    private UnaryOperator<String> validator;

    public FormField(String labelText, String placeholder, TextInputControl targetField) {
        this.inputField = targetField;
        this.inputField.setPromptText(placeholder);

        this.titleLabel = new Label(labelText);
        this.titleLabel.getStyleClass().add("label-field");

        if (labelText == null || labelText.trim().isEmpty()) {
            this.titleLabel.setVisible(false);
            this.titleLabel.setManaged(false);
        }

        this.errorLabel = new Label();
        this.errorLabel.getStyleClass().add("label-error");

        this.errorLabel.setVisible(false);
        this.errorLabel.setManaged(false);

        this.setSpacing(4);
        this.getChildren().addAll(this.titleLabel, this.inputField, this.errorLabel);

        this.inputField.textProperty().addListener((obs, oldVal, newVal) -> clearError());

        this.inputField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (Boolean.FALSE.equals(isNowFocused)) {
                validate();
            }
        });
    }

    public String getText() {
        return inputField.getText();
    }

    public void setText(String text) {
        inputField.setText(text);
    }

    public void clear() {
        inputField.clear();
        clearError();
    }

    public void setValidator(UnaryOperator<String> validator) {
        this.validator = validator;
    }

    // returns true if valid, false if there is an error
    public boolean validate() {
        if (validator == null) return true;

        String errorMessage = validator.apply(inputField.getText());
        if (errorMessage != null) {
            setError(errorMessage);
            return false;
        } else {
            clearError();
            return true;
        }
    }

    public void setError(String errorMessage) {
        errorLabel.setText(errorMessage);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        if (!inputField.getStyleClass().contains(INPUT_ERROR_CLASS)) {
            inputField.getStyleClass().add(INPUT_ERROR_CLASS);
        }
    }

    public void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        inputField.getStyleClass().remove(INPUT_ERROR_CLASS);
    }
}