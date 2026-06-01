package com.example.fitplannerclient.ui.cli;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.util.ValidationUtils;

import java.util.List;

public class ProfileCli implements CliView {
    CliEngine engine;
    OutputPrinter printer;
    InputReader reader;
    ProfileManager profileManager;

    @Override
    public CliView execute(CliEngine engine) {
        this.engine = engine;
        printer = engine.getPrinter();
        reader = engine.getInput();

        profileManager = engine.getControllerFactory().createProfileManager();

        ProfileBean.ProfileType type = profileManager.getProfileInfoAsync().join().getProfileType();
        String msg = "";
        if (type == ProfileBean.ProfileType.ATHLETE) {
            msg = "Aggiungi trainer";
        } else {
            msg = "Visualizza codice d'invito";
        }

        printer.printHeader("PROFILO");
        printer.printMenu(null, List.of("Indietro", "Visualizza", "Modifica", msg, "Logout"));

        int scelta = reader.readInt("Scegli un'opzione: ", 1, 5);

        if (scelta == 1) {
            return new DashboardCli();
        } else if (scelta == 2) {
            printProfileData();
        } else if (scelta == 3) {
            updateProfileData();
        } else if (scelta == 4) {
            if (type == ProfileBean.ProfileType.ATHLETE) {
                useTrainerCode();
            } else {
                printInvitationCode();
            }
        } else if (scelta == 5) {
            engine.getSessionManager().logout();
            return new AuthenticationCli();
        }

        return this;
    }

    @Override
    public void stop() {
        // Intenzionalmente vuoto
    }

    public void printProfileData() {
        try {
            var profile = profileManager.getProfileInfoAsync().join();
            String[] headers = {"Campo", "Valore"};
            String[][] data = {
                    {"Nome", profile.getFirstName()},
                    {"Cognome", profile.getLastName()},
                    {"Email di contatto", profile.getContactEmail()},
                    {"Numero di telefono", profile.getPhoneNumber()},
            };
            printer.printTable(headers, data);
        } catch (Exception ex) {
            printer.printException("Errore nel recupero del profilo: ", ex);
        }

        reader.waitForEnter();
    }

    public void updateProfileData() {
        try{
            var profile = profileManager.getProfileInfoAsync().join();
            String name = reader.readStringAndValidate("Inserisci il nuovo nome", input -> ValidationUtils.validateName(input, "Nome", 50), profile.getFirstName());
            String surname = reader.readStringAndValidate("Inserisci il nuovo cognome", input -> ValidationUtils.validateName(input, "Cognome", 50), profile.getLastName());
            String email = reader.readStringAndValidate("Inserisci la nuova email di contatto", ValidationUtils::validateEmail, profile.getContactEmail());
            String phone = reader.readStringAndValidate("Inserisci il nuovo numero di telefono", ValidationUtils::validatePhone, profile.getPhoneNumber());

            profile.setFirstName(name);
            profile.setLastName(surname);
            profile.setContactEmail(email);
            profile.setPhoneNumber(phone);

            profileManager.updateProfileInfoAsync(profile).join();
            printer.printSuccess("Profilo aggiornato con successo!");
        } catch (Exception ex) {
            printer.printException("Errore durante l'aggiornamento del profilo: ", ex);
        }

        printProfileData();
    }

    public void printInvitationCode() {
        try {
            String code = profileManager.getInvitationCodeAsync().join();
            printer.printSuccess("Codice d'invito: " + code);
        } catch (Exception ex) {
            printer.printException("Errore nel recupero del codice d'invito: ", ex);
        }

        reader.waitForEnter();
    }

    public void useTrainerCode() {

        String invitationCode = reader.readString("Inserisci il codice d'invito: ");

        try {
            profileManager.linkTrainerAsync(invitationCode).join();
            printer.printSuccess("Trainer collegato con successo!");
        } catch (Exception ex) {
            printer.printException("Errore nel recupero del codice d'invito: ", ex);
        }

        reader.waitForEnter();
    }


}
