package com.example.fitplannerclient.ui.fx.view.auth;

import com.example.fitplannerclient.ui.fx.components.FormField;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AuthenticationView extends BorderPane {

    // --- Login Fields ---
    private final FormField loginEmailField = new FormField("Email", "Inserisci la tua email", new TextField());
    private final FormField loginPasswordField = new FormField("Password", "Inserisci la tua password", new PasswordField());

    // --- Registration Fields ---
    private final FormField regFirstNameField = new FormField("Nome", "Inserisci il tuo nome", new TextField());
    private final FormField regLastNameField = new FormField("Cognome", "Inserisci il tuo cognome", new TextField());
    private final FormField regContactEmailField = new FormField("Email di contatto", "Inserisci la tua email di contatto", new TextField());
    private final FormField regPhoneField = new FormField("Numero di telefono", "Inserisci il tuo numero di telefono", new TextField());

    private final FormField regEmailField = new FormField("Email", "Scegli un username", new TextField());
    private final FormField regPasswordField = new FormField("Password", "Scegli una password", new PasswordField());
    private final FormField regConfirmPasswordField = new FormField("Conferma Password", "Ripeti la password", new PasswordField());

    private final ComboBox<String> roleComboBox = new ComboBox<>();

    private final Button btnBack = new Button("Indietro");
    private final Button btnRegistration = new Button("Registrati");
    private final Button btnLogin = new Button("Accedi");
    private Button btnNext;

    public AuthenticationView() {
        btnBack.getStyleClass().add("button-secondary");
        btnRegistration.getStyleClass().add("button-primary");
        btnLogin.getStyleClass().add("button-primary");

        roleComboBox.getItems().addAll("Atleta", "Trainer");

        btnBack.setOnAction(e -> showSelectionMenu());

        showSelectionMenu();
    }

    public void showSelectionMenu() {
        this.btnLogin.setDefaultButton(false);
        this.btnRegistration.setDefaultButton(false);

        Label title = new Label("FIT PLANNER");
        title.getStyleClass().add("brand-logo");

        VBox card = createChooseLoginOrRegisterForm();
        styleCard(card);

        VBox content = new VBox(24);
        content.getChildren().addAll(title, card);

        content.setAlignment(Pos.CENTER);
        setCenterWithScroll(content);
    }

    public void showLoginForm() {
        clearFields();
        this.btnNext = this.btnLogin;
        this.btnLogin.setDefaultButton(true);
        this.btnRegistration.setDefaultButton(false);

        VBox fields = new VBox(10);
        fields.getChildren().addAll(loginEmailField, loginPasswordField);

        VBox loginForm = buildFinalLayout("Bentornato!", fields);
        styleCard(loginForm);
        setCenterWithScroll(loginForm);
    }

    public void showRegistrationForm() {
        clearFields();
        this.btnNext = this.btnRegistration;
        this.btnLogin.setDefaultButton(false);
        this.btnRegistration.setDefaultButton(true);

        VBox profileFields = new VBox(10);
        profileFields.getChildren().addAll(regFirstNameField, regLastNameField, regContactEmailField, regPhoneField);

        VBox registrationFields = new VBox(10);
        registrationFields.getChildren().addAll(
                createComboField("Ruolo", "Scegli il tuo ruolo", roleComboBox),
                regEmailField, regPasswordField, regConfirmPasswordField
        );

        Separator separator = new Separator();
        VBox.setMargin(separator, new Insets(15, 0, 15, 0));

        VBox fields = new VBox(10);
        fields.getChildren().addAll(profileFields, separator, registrationFields);

        VBox registrationForm = buildFinalLayout("Nuovo Account", fields);
        styleCard(registrationForm);
        setCenterWithScroll(registrationForm);
    }

    private void styleCard(VBox card) {
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

    private VBox createComboField(String label, String placeholder, ComboBox<String> targetBox) {
        Label labelField = new Label(label);
        labelField.getStyleClass().add("label-field");

        targetBox.setPromptText(placeholder);
        targetBox.setMaxWidth(Double.MAX_VALUE);

        VBox fieldGroup = new VBox(2);
        fieldGroup.getChildren().addAll(labelField, targetBox);
        return fieldGroup;
    }

    private void clearFields() {
        loginEmailField.clearError();
        loginPasswordField.clearError();

        regFirstNameField.clearError();
        regLastNameField.clearError();
        regContactEmailField.clearError();
        regPhoneField.clearError();
        regEmailField.clearError();
        regPasswordField.clearError();
        regConfirmPasswordField.clearError();

        roleComboBox.getSelectionModel().clearSelection();
    }

    // --- Actions ---
    public void setRegistrationBtnAction(Runnable action) { btnRegistration.setOnAction(event -> action.run()); }
    public void setLoginBtnAction(Runnable action) { btnLogin.setOnAction(event -> action.run()); }

    // --- Getters ---
    public FormField getLoginEmailField() { return loginEmailField; }
    public FormField getLoginPasswordField() { return loginPasswordField; }
    public FormField getRegFirstNameField() { return regFirstNameField; }
    public FormField getRegLastNameField() { return regLastNameField; }
    public FormField getRegContactEmailField() { return regContactEmailField; }
    public FormField getRegPhoneField() { return regPhoneField; }
    public FormField getRegEmailField() { return regEmailField; }
    public FormField getRegPasswordField() { return regPasswordField; }
    public FormField getRegConfirmPasswordField() { return regConfirmPasswordField; }

    public String getLoginEmail() { return loginEmailField.getText(); }
    public String getLoginPassword() { return loginPasswordField.getText(); }

    public String getRegFirstName() { return regFirstNameField.getText(); }
    public String getRegLastName() { return regLastNameField.getText(); }
    public String getRegContactEmail() { return regContactEmailField.getText(); }
    public String getRegPhoneNumber() { return regPhoneField.getText(); }
    public String getRegEmail() { return regEmailField.getText(); }
    public String getRegPassword() { return regPasswordField.getText(); }

    public String getRole() { return roleComboBox.getValue(); }
}