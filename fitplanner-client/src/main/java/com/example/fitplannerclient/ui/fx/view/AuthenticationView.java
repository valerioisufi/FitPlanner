package com.example.fitplannerclient.ui.fx.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AuthenticationView extends BorderPane {

    private final TextField emailFieldInput = new TextField();
    private final PasswordField passwordFieldInput = new PasswordField();
    private final PasswordField confirmPasswordFieldInput = new PasswordField();

    private final TextField firstNameFieldInput = new TextField();
    private final TextField lastNameFieldInput = new TextField();
    private final TextField contactEmailFieldInput = new TextField();
    private final TextField phoneNumberFieldInput = new TextField();

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
        this.btnLogin.setDefaultButton(false);
        this.btnRegistration.setDefaultButton(false);

        Label title = new Label("FitPlanner");
        title.getStyleClass().add("brand-logo");

        VBox card = createChooseLoginOrRegisterForm();
        styleCard(card);

        VBox content = new VBox(10);
        content.getChildren().addAll(title, card);

        content.setAlignment(Pos.CENTER);
        setCenterWithScroll(content);
    }

    public void showLoginForm() {
        clearFields();
        this.btnNext = this.btnLogin;

        this.btnLogin.setDefaultButton(true);
        this.btnRegistration.setDefaultButton(false);

        VBox loginForm = createLoginForm();
        styleCard(loginForm);
        setCenterWithScroll(loginForm);
    }

    public void showRegistrationForm() {
        clearFields();
        this.btnNext = this.btnRegistration;

        this.btnLogin.setDefaultButton(false);
        this.btnRegistration.setDefaultButton(true);

        VBox registrationForm = createRegistrationForm();
        styleCard(registrationForm);
        setCenterWithScroll(registrationForm);
    }

    private void styleCard(VBox card) {
        // Apply the card style defined in the CSS
        card.getStyleClass().add("card");
        card.setMinWidth(380);
        card.setMaxWidth(400);
        card.setMaxHeight(Region.USE_PREF_SIZE);

    }

    private void setCenterWithScroll(VBox content) {
        VBox wrapper = new VBox(content);
        wrapper.setAlignment(Pos.CENTER);

        wrapper.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(wrapper);

        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        scrollPane.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");

        this.setCenter(scrollPane);
    }

    private VBox createChooseLoginOrRegisterForm() {
        VBox formContainer = new VBox(25);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(40, 30, 40, 30));

        Label subtitle = new Label("Gestisci i tuoi allenamenti in modo semplice");
        subtitle.getStyleClass().add("body-base");
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);

        VBox buttons = new VBox(15);
        buttons.setAlignment(Pos.CENTER);

        Button btnGoToLogin = new Button("Accedi");
        btnGoToLogin.getStyleClass().add("button-primary");
        btnGoToLogin.setMaxWidth(Double.MAX_VALUE);
        btnGoToLogin.setOnAction(e -> showLoginForm());

        Button btnGoToRegister = new Button("Crea Account");
        btnGoToRegister.getStyleClass().add("button-secondary");
        btnGoToRegister.setMaxWidth(Double.MAX_VALUE);
        btnGoToRegister.setOnAction(e -> showRegistrationForm());

        buttons.getChildren().addAll(btnGoToLogin, btnGoToRegister);
        formContainer.getChildren().addAll(subtitle, buttons);

        return formContainer;
    }

    private VBox createLoginForm() {
        VBox fields = new VBox(10);
        fields.getChildren().addAll(
                createField("Email", "Inserisci la tua email", emailFieldInput),
                createField("Password", "Inserisci la tua password", passwordFieldInput)
        );

        return buildFinalLayout("Bentornato!", fields);
    }

    private VBox createRegistrationForm() {
        VBox profileFields = new VBox(10);
        profileFields.getChildren().addAll(
                createField("Nome", "Inserisci il tuo nome", firstNameFieldInput),
                createField("Cognome", "Inserisci il tuo cognome", lastNameFieldInput),
                createField("Email di contatto", "Inserisci la tua email di contatto", contactEmailFieldInput),
                createField("Numero di telefono", "Inserisci il tuo numero di telefono", phoneNumberFieldInput)
        );

        VBox registrationFields = new VBox(10);
        registrationFields.getChildren().addAll(
                createField("Email", "Scegli un username", emailFieldInput),
                createField("Password", "Scegli una password", passwordFieldInput),
                createField("Conferma Password", "Ripeti la password", confirmPasswordFieldInput)
        );

        Separator separator = new Separator();
        VBox.setMargin(separator, new Insets(15, 0, 15, 0));

        VBox fields = new VBox(10);
        fields.getChildren().addAll(profileFields, separator, registrationFields);

        return buildFinalLayout("Nuovo Account", fields);
    }

    private VBox buildFinalLayout(String titleStr, VBox fields) {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);

        Label title = new Label(titleStr);
        title.getStyleClass().add("heading-h2");

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
        emailFieldInput.clear();
        passwordFieldInput.clear();
        confirmPasswordFieldInput.clear();

        firstNameFieldInput.clear();
        lastNameFieldInput.clear();
        contactEmailFieldInput.clear();
        phoneNumberFieldInput.clear();
    }

    public void setRegistrationBtnAction(Runnable action) {
        btnRegistration.setOnAction(event -> action.run());
    }

    public void setLoginBtnAction(Runnable action) {
        btnLogin.setOnAction(event -> action.run());
    }

    public String getEmail() { return emailFieldInput.getText(); }
    public String getPassword() { return passwordFieldInput.getText(); }
    public String getConfirmPassword() { return confirmPasswordFieldInput.getText(); }

    public String getFirstName() { return firstNameFieldInput.getText(); }
    public String getLastName() { return lastNameFieldInput.getText(); }
    public String getContactEmail() { return contactEmailFieldInput.getText(); }
    public String getPhoneNumber() { return phoneNumberFieldInput.getText(); }
}