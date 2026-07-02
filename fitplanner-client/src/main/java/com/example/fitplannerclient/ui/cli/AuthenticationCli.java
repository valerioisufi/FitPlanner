package com.example.fitplannerclient.ui.cli;

import com.example.fitplannerclient.bean.auth.LoginBean;
import com.example.fitplannerclient.bean.auth.RegisterBean;
import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.SessionController;
import com.example.fitplannerclient.util.ValidationUtils;

import java.util.List;

public class AuthenticationCli implements CliView {

    CliEngine engine;
    OutputPrinter printer;
    InputReader reader;

    SessionController sessionController;

    @Override
    public CliView execute(CliEngine engine) {
        this.engine = engine;
        printer = engine.getPrinter();
        reader = engine.getInput();

        sessionController = engine.getSessionController();

        printer.printHeader("AUTENTICAZIONE");
        printer.printMenu(null, List.of("Accedi", "Registrati", "Esci"));

        int scelta = reader.readInt("Scegli un'opzione: ", 1, 3);

        if (scelta == 1) {
            return login();
        } else if (scelta == 2) {
            return register();
        } else if (scelta == 3) {
            return null; // Esci dall'app
        }

        return this;
    }

    private CliView login() {
        String email = reader.readStringAndValidate("Email: ", ValidationUtils::validateEmail);
        String password = reader.readString("Password: ");

        LoginBean loginBean = new LoginBean(email, password);

        try {
            sessionController.loginAsync(loginBean).join();
            printer.printSuccess("Login effettuato con successo!");
            return new DashboardCli();
        } catch (Exception ex) {
            String errorMsg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            printer.printError("Errore nel login: " + errorMsg);
            return this; // Riprova a fare il login o la registrazione
        }
    }

    private CliView register() {
        String name = reader.readStringAndValidate("Nome: ", input -> ValidationUtils.validateName(input, "Nome", 50));
        String surname = reader.readStringAndValidate("Cognome: ", input -> ValidationUtils.validateName(input, "Cognome", 50));
        String contactEmail = reader.readStringAndValidate("Email di contatto: ", ValidationUtils::validateEmail);
        String phoneNumber = reader.readStringAndValidate("Numero di telefono: ", ValidationUtils::validatePhone);

        printer.printMenu(null, List.of("ATHLETE", "TRAINER"));
        int roleInt = reader.readInt("Scegli un ruolo: ", 1, 2);
        ProfileBean.ProfileType profileType = switch (roleInt) {
            case 1 -> ProfileBean.ProfileType.ATHLETE;
            case 2 -> ProfileBean.ProfileType.TRAINER;
            default -> throw new IllegalStateException("Scelta non valida: " + roleInt);
        };

        String email = reader.readStringAndValidate("Email: ", ValidationUtils::validateEmail);
        String password = reader.readStringAndValidate("Password: ", ValidationUtils::validatePassword);
        reader.readStringAndValidate("Conferma password: ", confirm ->
                ValidationUtils.validatePasswordMatch(password, confirm)
        );

        ProfileBean profile = new ProfileBean();
        profile.setFirstName(name);
        profile.setLastName(surname);
        profile.setContactEmail(contactEmail);
        profile.setPhoneNumber(phoneNumber);

        profile.setProfileType(profileType);

        RegisterBean registerBean = new RegisterBean();
        registerBean.setEmail(email);
        registerBean.setPassword(password);
        registerBean.setProfile(profile);

        try {
            sessionController.registerAsync(registerBean).join();
            printer.printSuccess("Registrazione effettuata con successo!");
            return new DashboardCli();
        } catch (Exception ex) {
            String errorMsg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
            printer.printError("Errore durante la registrazione: " + errorMsg);
            return this;
        }
    }

    @Override
    public void stop() { 
        // Intenzionalmente vuoto
    }
}
