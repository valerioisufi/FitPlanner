package com.example.fitplannerclient.ui.cli;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.AuthManager;
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

        printer.printHeader("PROFILO");
        printer.printMenu(null, List.of("Indietro", "Visualizza", "Modifica", "logout"));

        int scelta = reader.readInt("Scegli un'opzione: ", 1, 4);

        if (scelta == 1) {
            new DashboardCli();
        } else if (scelta == 2) {
            printProfileData();
        } else if (scelta == 3) {
            updateProfileData();
        } else if (scelta == 4) {
            engine.getSessionManager().logout();
        }

        return this;
    }

    @Override
    public void stop() {}

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
        String name = reader.readStringAndValidate("Inserisci il nuovo nome: ", input -> ValidationUtils.validateName(input, "Nome", 50));
        String surname = reader.readStringAndValidate("Inserisci il nuovo cognome: ", input -> ValidationUtils.validateName(input, "Cognome", 50));
        String email = reader.readStringAndValidate("Inserisci la nuova email di contatto: ", ValidationUtils::validateEmail);
        String phone = reader.readStringAndValidate("Inserisci il nuovo numero di telefono: ", ValidationUtils::validatePhone);

        ProfileBean profileBean = new ProfileBean();
        profileBean.setFirstName(name);
        profileBean.setLastName(surname);
        profileBean.setContactEmail(email);
        profileBean.setPhoneNumber(phone);

        try{
            profileManager.updateProfileInfoAsync(profileBean).join();
            printer.printSuccess("Profilo aggiornato con successo!");
        } catch (Exception ex) {
            printer.printException("Errore durante l'aggiornamento del profilo: ", ex);
        }
        
        printProfileData();
    }


}
