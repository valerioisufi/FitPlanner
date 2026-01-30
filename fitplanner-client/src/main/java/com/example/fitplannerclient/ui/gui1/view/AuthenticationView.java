package com.example.fitplannerclient.ui.gui1.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.*;

public class AuthenticationView extends BaseView {

    private final TextField usernameFieldInput = new TextField();
    private final PasswordField passwordFieldInput = new PasswordField();
    private final PasswordField confirmPasswordFieldInput = new PasswordField();

    private final Button btnBack = new Button("Indietro");
    private final Button btnRegistration = new Button("Registrati");
    private final Button btnLogin = new Button("Accedi");
    private Button btnNext;

    public AuthenticationView() {
        btnBack.getStyleClass().add("button-secondary");
        btnRegistration.getStyleClass().add("button-primary");
        btnLogin.getStyleClass().add("button-primary");

        btnBack.setOnAction(e -> {
            showSelectionMenu();
        });

        showSelectionMenu();
    }

    public void showSelectionMenu() {
        this.getChildren().clear();
        VBox card = createChooseLoginOrRegisterForm();
        styleCard(card);
        this.getChildren().add(card);
    }

    public void showLoginForm() {
        this.getChildren().clear();
        clearFields();
        this.btnNext = this.btnLogin;

        VBox loginForm = createLoginForm();
        styleCard(loginForm);
        this.getChildren().add(loginForm);
    }

    public void showRegistrationForm() {
        this.getChildren().clear();
        clearFields();
        this.btnNext = this.btnRegistration;

        VBox registrationForm = createRegistrationForm();
        styleCard(registrationForm);
        this.getChildren().add(registrationForm);
    }

    private void styleCard(VBox card) {
        card.getStyleClass().add("card");
        card.setMinWidth(380);
        card.setMaxWidth(400);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(card, Pos.CENTER);
    }

    private VBox createChooseLoginOrRegisterForm() {
        VBox formContainer = new VBox(25);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(40, 30, 40, 30));

        Label title = new Label("FitPlanner");
        title.getStyleClass().add("h1");

        Label subtitle = new Label("Gestisci i tuoi allenamenti in modo semplice");
        subtitle.getStyleClass().add("paragraph");
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);

        VBox buttons = new VBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button btnLogin = new Button("Accedi");
        btnLogin.getStyleClass().add("button-primary");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setOnAction(e -> showLoginForm());

        Button btnRegister = new Button("Crea Account");
        btnRegister.getStyleClass().add("button-secondary"); // Secondario per differenziarlo
        btnRegister.setMaxWidth(Double.MAX_VALUE);
        btnRegister.setOnAction(e -> showRegistrationForm());

        buttons.getChildren().addAll(btnLogin, btnRegister);
        formContainer.getChildren().addAll(title, subtitle, buttons);

        return formContainer;
    }

    private VBox createLoginForm() {
        VBox fields = new VBox(10);
        fields.getChildren().addAll(
                createField("Username", "Inserisci il tuo username", usernameFieldInput),
                createField("Password", "Inserisci la tua password", passwordFieldInput)
        );

        return buildFinalLayout("Bentornato!", fields);
    }

    private VBox createRegistrationForm() {
        VBox fields = new VBox(10);
        fields.getChildren().addAll(
                createField("Username", "Scegli un username", usernameFieldInput),
                createField("Password", "Scegli una password", passwordFieldInput),
                createField("Conferma Password", "Ripeti la password", confirmPasswordFieldInput)
        );

        return buildFinalLayout("Nuovo Account", fields);
    }

    private VBox buildFinalLayout(String titleStr, VBox fields) {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));

        Label title = new Label(titleStr);
        title.getStyleClass().add("h2");

        layout.getChildren().addAll(title, fields, createButtonBox());
        return layout;
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttonBox.getChildren().addAll(btnBack, spacer, btnNext);
        return buttonBox;
    }

    private VBox createField(String labelText, String placeholder, TextField targetField) {
        Label label = new Label(labelText);
        label.getStyleClass().add("label-field");

        targetField.setPromptText(placeholder);

        VBox fieldGroup = new VBox(2);
        fieldGroup.getChildren().addAll(label, targetField);
        return fieldGroup;
    }

    private void clearFields() {
        usernameFieldInput.clear();
        passwordFieldInput.clear();
        confirmPasswordFieldInput.clear();
    }

    public void setRegistrationBtnAction(Runnable action) {
        btnRegistration.setOnAction(event -> action.run());
    }

    public void setLoginBtnAction(Runnable action) {
        btnLogin.setOnAction(event -> action.run());
    }

    public String getUsername() { return usernameFieldInput.getText(); }
    public String getPassword() { return passwordFieldInput.getText(); }
    public String getConfirmPassword() { return confirmPasswordFieldInput.getText(); }
}