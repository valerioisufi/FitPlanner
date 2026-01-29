package com.example.fitplannerclient.ui.gui1.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

public class AuthenticationView extends BaseView {

    private final TextField usernameFieldInput = new TextField();
    private final TextField passwordFieldInput = new TextField();

    private final Button btnBack = new Button("Back");
    private final Button btnNext = new Button("Next");

    public AuthenticationView() {
        VBox registrationCard = createLoginForm();
        StackPane.setAlignment(registrationCard, Pos.CENTER);
        this.getChildren().add(registrationCard);
    }

    public void setBtnBackAction(Runnable action) {
        btnBack.setOnAction(event -> action.run());
    }

    public void setBtnNextAction(Runnable action) {
        btnNext.setOnAction(event -> action.run());
    }

    public Button getBtnNext() {
        return btnNext;
    }

    public String getUsername() {
        return usernameFieldInput.getText();
    }

    public String getPassword() {
        return passwordFieldInput.getText();
    }

    private VBox createLoginForm() {
        VBox formContainer = new VBox();

        VBox usernameField = createField("Username", "Inserisci lo username", this.usernameFieldInput);
        VBox passwordField = createField("Password", "Inserisci la tua password", this.passwordFieldInput);

        formContainer.getChildren().addAll(usernameField, passwordField);

        Separator separator = new Separator();
        separator.setPadding(new Insets(20, 0, 20, 0));

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        btnBack.getStyleClass().add("button-secondary");
        btnNext.getStyleClass().add("button-primary");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttonBox.getChildren().addAll(btnBack, spacer, btnNext);

        VBox card = new VBox();
        card.getStyleClass().add("card");

        card.setMinWidth(300);
        card.setMaxWidth(400);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        card.getChildren().addAll(formContainer, separator, buttonBox);

        return card;
    }

    private VBox createField(String labelText, String placeholder, TextField targetField) {
        Label label = new Label(labelText);
        label.getStyleClass().add("label-field");

        targetField.setPromptText(placeholder);
        targetField.getStyleClass().add("text-field");

        VBox fieldGroup = new VBox(5);
        fieldGroup.getChildren().addAll(label, targetField);
        return fieldGroup;
    }
}