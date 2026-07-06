package com.example.fitplannerclient.ui.cli;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.util.ValidationUtils;

import java.util.Arrays;
import java.util.List;

public class ProfileCli extends AbstractCliView {

    private ProfileManager profileManager;

    @Override
    protected CliView render() {
        profileManager = engine.getSessionContext().createProfileManager();

        ProfileBean.ProfileType type = profileManager.getProfileInfoAsync().join().getProfileType();
        String extra = (type == ProfileBean.ProfileType.ATHLETE) ? "Aggiungi trainer" : "Visualizza codice d'invito";

        printer.printHeader("PROFILO");
        printer.printMenu(null, List.of("Indietro", "Visualizza", "Modifica", extra, "Logout"));

        int scelta = reader.readInt("Scegli un'opzione: ", 1, 5);

        switch (scelta) {
            case 1 -> { return new DashboardCli(); }
            case 2 -> printProfileData();
            case 3 -> updateProfileData();
            case 4 -> {
                if (type == ProfileBean.ProfileType.ATHLETE) {
                    useTrainerCode();
                } else {
                    printInvitationCode();
                }
            }
            case 5 -> {
                engine.getSessionManager().logout();
                return new AuthenticationCli();
            }
        }

        return this;
    }

    private void printProfileData() {
        try {
            ProfileBean profile = profileManager.getProfileInfoAsync().join();
            List<String> headers = Arrays.asList("Campo", "Valore");
            List<List<String>> data = Arrays.asList(
                    Arrays.asList("Nome", profile.getFirstName() != null ? profile.getFirstName() : ""),
                    Arrays.asList("Cognome", profile.getLastName() != null ? profile.getLastName() : ""),
                    Arrays.asList("Email di contatto", profile.getContactEmail() != null ? profile.getContactEmail() : ""),
                    Arrays.asList("Numero di telefono", profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "")
            );
            printer.printTable(headers, data);
        } catch (Exception ex) {
            printer.printException("Errore nel recupero del profilo: ", ex);
        }

        reader.waitForEnter();
    }

    private void updateProfileData() {
        try {
            ProfileBean profile = profileManager.getProfileInfoAsync().join();
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

    private void printInvitationCode() {
        try {
            String code = profileManager.getInvitationCodeAsync().join();
            printer.printSuccess("Codice d'invito: " + code);
        } catch (Exception ex) {
            printer.printException("Errore nel recupero del codice d'invito: ", ex);
        }

        reader.waitForEnter();
    }

    private void useTrainerCode() {
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
