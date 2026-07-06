package com.example.fitplannerclient.ui.cli.trainercli;

import com.example.fitplannerclient.bean.plan.WorkoutPlanSummaryBean;
import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.ui.cli.AbstractCliView;
import com.example.fitplannerclient.ui.cli.CliView;
import com.example.fitplannerclient.ui.cli.DashboardCli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WorkoutPlanLibraryCli extends AbstractCliView {

    private WorkoutPlanManager planManager;
    private ProfileManager profileManager;

    @Override
    protected CliView render() {
        this.planManager = engine.getSessionContext().createWorkoutPlanManager();
        this.profileManager = engine.getSessionContext().createProfileManager();

        printer.printHeader("LIBRERIA PIANI DI ALLENAMENTO");
        printer.printMenu(null, List.of(
                "Indietro",
                "Visualizza i miei Piani",
                "Crea Nuovo Piano",
                "Modifica Piano",
                "Clona Piano",
                "Elimina Piano",
                "Assegna Piano a un Atleta"
        ));

        int scelta = reader.readInt("Scegli un'opzione: ", 1, 7);

        switch (scelta) {
            case 1 -> { return new DashboardCli(); }
            case 2 -> visualizzaPiani();
            case 3 -> { return new PlanEditorCli(null, false); }
            case 4 -> {
                String planId = selezionaPiano("Modifica");
                if (planId != null) return new PlanEditorCli(planId, false);
            }
            case 5 -> {
                String planId = selezionaPiano("Clona");
                if (planId != null) return new PlanEditorCli(planId, true);
            }
            case 6 -> eliminaPiano();
            case 7 -> assegnaPiano();
        }

        return this;
    }

    private void visualizzaPiani() {
        try {
            List<WorkoutPlanSummaryBean> plans = planManager.getMyCreatedPlansSummaryAsync().join();
            if (plans == null || plans.isEmpty()) {
                printer.printInfo("Nessun piano trovato.");
            } else {
                List<ProfileBean> athletes = profileManager.getMyAthletesAsync().join();

                List<String> headers = Arrays.asList("Titolo Piano", "Assegnato A");
                List<List<String>> data = new ArrayList<>();
                for (WorkoutPlanSummaryBean plan : plans) {
                    String assignedTo = "Nessuno";
                    if (plan.getAssignedTo() != null) {
                        ProfileBean athlete = getAthleteById(athletes, plan.getAssignedTo());
                        assignedTo = (athlete != null)
                                ? athlete.getFirstName() + " " + athlete.getLastName()
                                : plan.getAssignedTo();
                    }
                    data.add(Arrays.asList(plan.getPlanTitle() != null ? plan.getPlanTitle() : "Senza Titolo", assignedTo));
                }
                printer.printTable(headers, data);
            }
        } catch (Exception e) {
            printer.printException("Errore durante il caricamento dei piani: ", e);
        }
        reader.waitForEnter();
    }

    private ProfileBean getAthleteById(List<ProfileBean> athletes, String id) {
        if (athletes == null) return null;
        return athletes.stream().filter(a -> id.equals(a.getUserId())).findFirst().orElse(null);
    }

    private String selezionaPiano(String action) {
        try {
            List<WorkoutPlanSummaryBean> plans = planManager.getMyCreatedPlansSummaryAsync().join();
            if (plans == null || plans.isEmpty()) {
                printer.printInfo("Nessun piano trovato.");
                reader.waitForEnter();
                return null;
            }
            return reader.selectFrom("Seleziona un piano da " + action.toLowerCase() + ":", plans,
                            p -> p.getPlanTitle() != null ? p.getPlanTitle() : "Senza Titolo")
                    .map(WorkoutPlanSummaryBean::getPlanId)
                    .orElse(null);
        } catch (Exception e) {
            printer.printException("Errore durante il caricamento dei piani: ", e);
            reader.waitForEnter();
            return null;
        }
    }

    private void eliminaPiano() {
        String planId = selezionaPiano("Eliminare");
        if (planId != null) {
            try {
                planManager.deletePlanAsync(planId).join();
                printer.printSuccess("Piano eliminato con successo!");
            } catch (Exception e) {
                printer.printException("Errore durante l'eliminazione: ", e);
            }
            reader.waitForEnter();
        }
    }

    private void assegnaPiano() {
        String planId = selezionaPiano("Assegnare");
        if (planId == null) return;

        try {
            List<ProfileBean> athletes = profileManager.getMyAthletesAsync().join();
            if (athletes == null || athletes.isEmpty()) {
                printer.printInfo("Non hai atleti a cui assegnare il piano.");
                reader.waitForEnter();
                return;
            }

            Optional<ProfileBean> selected = reader.selectFrom("Seleziona l'atleta a cui assegnare il piano:", athletes,
                    a -> a.getFirstName() + " " + a.getLastName() + " (" + a.getContactEmail() + ")");
            if (selected.isEmpty()) return;

            planManager.assignPlanToAthleteAsync(planId, selected.get().getUserId()).join();
            printer.printSuccess("Piano assegnato con successo!");

        } catch (Exception e) {
            printer.printException("Errore durante l'assegnazione: ", e);
        }
        reader.waitForEnter();
    }
}
