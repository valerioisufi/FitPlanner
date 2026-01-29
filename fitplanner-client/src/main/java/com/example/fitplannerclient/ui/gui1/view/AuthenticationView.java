package com.example.fitplannerclient.ui.gui1.view;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class AuthenticationView extends BaseView {
    public AuthenticationView() {
    }

    private VBox createField(String labelText, String placeholder) {
        Label label = new Label(labelText);
        label.getStyleClass().add("label-field");

        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        textField.getStyleClass().add("text-field"); // Applica lo stile CSS

        VBox fieldGroup = new VBox(5); // 5px spazio tra label e input
        fieldGroup.getChildren().addAll(label, textField);
        return fieldGroup;
    }
}
