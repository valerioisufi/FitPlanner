package com.example.fitplannerclient.ui.fx.view;

import com.example.fitplannerclient.ui.gui1.view.BaseView;
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
        // Apply secondary and primary button styles from typography.css/colors.css
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

        VBox card = createChooseLoginOrRegisterForm();
        styleCard(card);
        this.setContent(card);
    }

    public void showLoginForm() {
        clearFields();
        this.btnNext = this.btnLogin;

        this.btnLogin.setDefaultButton(true);
        this.btnRegistration.setDefaultButton(false);

        VBox loginForm = createLoginForm();
        styleCard(loginForm);
        this.setContent(loginForm);
    }

    public void showRegistrationForm() {
        clearFields();
        this.btnNext = this.btnRegistration;

        this.btnLogin.setDefaultButton(false);
        this.btnRegistration.setDefaultButton(true);

        VBox registrationForm = createRegistrationForm();
        styleCard(registrationForm);
        this.setContent(registrationForm);
    }

    private void styleCard(VBox card) {
        // Apply the card style defined in the CSS
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
        // Replaced "h1" with the correct typography class
        title.getStyleClass().add("heading-h1");

        Label subtitle = new Label("Gestisci i tuoi allenamenti in modo semplice");
        // Replaced "paragraph" with "body-base" to match the CSS
        subtitle.getStyleClass().add("body-base");
        subtitle.setWrapText(true);
        subtitle.setAlignment(Pos.CENTER);

        VBox buttons = new VBox(15);
        buttons.setAlignment(Pos.CENTER);

        // Local buttons for navigation
        Button btnGoToLogin = new Button("Accedi");
        btnGoToLogin.getStyleClass().add("button-primary");
        btnGoToLogin.setMaxWidth(Double.MAX_VALUE);
        btnGoToLogin.setOnAction(e -> showLoginForm());

        Button btnGoToRegister = new Button("Crea Account");
        btnGoToRegister.getStyleClass().add("button-secondary");
        btnGoToRegister.setMaxWidth(Double.MAX_VALUE);
        btnGoToRegister.setOnAction(e -> showRegistrationForm());

        buttons.getChildren().addAll(btnGoToLogin, btnGoToRegister);
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
        // Replaced "h2" with the correct typography class
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
        // "label-field" correctly maps to the CSS class provided
        label.getStyleClass().add("label-field");

        targetField.setPromptText(placeholder);

        // JavaFX automatically adds ".text-field" and ".password-field" classes to instances of TextField and PasswordField.
        // Therefore, we do not need to add them manually, the CSS will pick them up.

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