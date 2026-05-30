package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.AuthManager;
import com.example.fitplannerclient.bean.auth.LoginBean;
import com.example.fitplannerclient.bean.auth.RegisterBean;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.view.auth.AuthenticationView;
import com.example.fitplannerclient.util.ValidationUtils;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

public class AuthenticationViewController implements GuiController {
    private final GuiManager guiManager;
    private final AuthManager authManager;
    private final AuthenticationView view;
    private final Runnable onLoginSuccess;

    public AuthenticationViewController(GuiManager guiManager, AuthManager authManager, Runnable onLoginSuccess) {
        this.guiManager = guiManager;
        this.authManager = authManager;
        this.onLoginSuccess = onLoginSuccess;
        this.view = new AuthenticationView();

        bindValidators();

        this.view.setLoginBtnAction(this::onLogin);
        this.view.setRegistrationBtnAction(this::onRegister);
    }

    private void bindValidators() {
        // Login Validation
        view.getLoginEmailField().setValidator(ValidationUtils::validateEmail);
        view.getLoginPasswordField().setValidator(confirm -> ValidationUtils.validateRequired(confirm, "Password", 32));

        // Registration Validation
        view.getRegFirstNameField().setValidator(name -> ValidationUtils.validateName(name, "Nome", 50));
        view.getRegLastNameField().setValidator(surname -> ValidationUtils.validateName(surname, "Cognome", 50));

        view.getRegContactEmailField().setValidator(ValidationUtils::validateEmail);
        view.getRegPhoneField().setValidator(ValidationUtils::validatePhone);

        view.getRegEmailField().setValidator(ValidationUtils::validateEmail);
        view.getRegPasswordField().setValidator(ValidationUtils::validatePassword);

        view.getRegConfirmPasswordField().setValidator(confirm ->
                ValidationUtils.validatePasswordMatch(view.getRegPassword(), confirm)
        );
    }

    private void onLogin() {
        // Evaluate validations separately to prevent short-circuiting so ALL fields show their errors at once
        boolean isEmailValid = view.getLoginEmailField().validate();
        boolean isPasswordValid = view.getLoginPasswordField().validate();
        boolean isValid = isEmailValid && isPasswordValid;

        if (!isValid) return; // Stop here if UI validation fails

        LoginBean loginBean = new LoginBean(this.view.getLoginEmail(), this.view.getLoginPassword());

        authManager.loginAsync(loginBean)
                .thenRun(onLoginSuccess)
                .exceptionally(ex -> {
                    this.guiManager.showExceptionError("Errore nel login:", ex);
                    return null;
                });
    }

    private void onRegister() {
        // Validate all fields simultaneously
        boolean isFirstNameValid = view.getRegFirstNameField().validate();
        boolean isLastNameValid = view.getRegLastNameField().validate();
        boolean isContactEmailValid = view.getRegContactEmailField().validate();
        boolean isPhoneValid = view.getRegPhoneField().validate();
        boolean isProfileValid = isFirstNameValid && isLastNameValid && isContactEmailValid && isPhoneValid;

        boolean isRegEmailValid = view.getRegEmailField().validate();
        boolean isRegPasswordValid = view.getRegPasswordField().validate();
        boolean isRegConfirmPasswordValid = view.getRegConfirmPasswordField().validate();
        boolean isAuthValid = isRegEmailValid && isRegPasswordValid && isRegConfirmPasswordValid;

        if (!isProfileValid || !isAuthValid) return;

        String selectRole = this.view.getRole();
        if (selectRole == null) {
            this.guiManager.showNotification(GuiManager.NotificationType.ERROR, "Attenzione, devi selezionare un ruolo per registrarti");
            return;
        }

        RegisterBean registerBean = new RegisterBean();
        registerBean.setEmail(this.view.getRegEmail());
        registerBean.setPassword(this.view.getRegPassword());

        ProfileBean profileBean = new ProfileBean();
        profileBean.setFirstName(this.view.getRegFirstName());
        profileBean.setLastName(this.view.getRegLastName());
        profileBean.setContactEmail(this.view.getRegContactEmail());
        profileBean.setPhoneNumber(this.view.getRegPhoneNumber());

        ProfileBean.ProfileType profileType = switch (selectRole.toLowerCase()) {
            case "atleta" -> ProfileBean.ProfileType.ATHLETE;
            case "trainer" -> ProfileBean.ProfileType.TRAINER;
            default -> throw new IllegalStateException("Ruolo non previsto: " + selectRole);
        };
        profileBean.setProfileType(profileType);
        registerBean.setProfile(profileBean);

        authManager.registerAsync(registerBean)
                .thenRun(onLoginSuccess)
                .exceptionally(ex -> {
                    this.guiManager.showExceptionError("Errore durante la registrazione:", ex);
                    return null;
                });
    }

    @Override
    public Pane getView() { return this.view; }

    @Override
    public void start() {}

    @Override
    public void stop() {}
}