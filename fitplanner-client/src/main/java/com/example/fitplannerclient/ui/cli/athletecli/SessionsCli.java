package com.example.fitplannerclient.ui.cli.athletecli;

import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.ui.cli.AbstractCliView;
import com.example.fitplannerclient.ui.cli.CliView;
import com.example.fitplannerclient.ui.cli.DashboardCli;

import java.util.ArrayList;
import java.util.List;

public class SessionsCli extends AbstractCliView {

    private static final String BACK_LABEL = "Indietro";
    private static final String CHOOSE_OPTION_PROMPT = "Scegli un'opzione: ";

    private WorkoutPlanManager planManager;

    @Override
    protected CliView render() {
        this.planManager = engine.getSessionContext().createWorkoutPlanManager();

        printer.printHeader("LE MIE SESSIONI");
        printer.printMenu(null, List.of(
                BACK_LABEL,
                "Sessione del giorno",
                "Visualizza tutte le sessioni"
        ));

        int scelta = reader.readInt(CHOOSE_OPTION_PROMPT, 1, 3);
        return switch (scelta) {
            case 1 -> new DashboardCli();
            case 2 -> todayPlan();
            case 3 -> allSessions();
            default -> this;
        };
    }

    private CliView allSessions() {
        WorkoutScheduleBean schedule = planManager.getCurrentCycleScheduleAsync()
                .exceptionally(ex -> {
                    printer.printException("Errore nel caricamento del piano di allenamento: ", ex);
                    return null;
                }).join();

        if (schedule == null || schedule.getDays() == null || schedule.getDays().isEmpty()) {
            printer.printInfo("Non hai ancora nessuna programmazione assegnata.");
            reader.waitForEnter();
            return this;
        }

        List<ScheduleDayBean> days = schedule.getDays();
        printer.printInfo("Ecco il tuo schedule di allenamento:");

        List<String> headers = List.of("Giorno", "Sessione", "Stato", "Oggi");
        List<List<String>> data = new ArrayList<>();
        List<ScheduleDayBean> selectableDays = new ArrayList<>();

        for (ScheduleDayBean day : days) {
            String sessionName = day.getSession() != null ? day.getSession().getName() : "Riposo";
            String stateStr = day.getState() != null ? day.getState().toString() : "Sconosciuto";
            String todayStr = day.isToday() ? "X" : "";

            data.add(List.of(String.valueOf(day.getAbsoluteDay()), sessionName, stateStr, todayStr));

            if (day.getSession() != null) {
                selectableDays.add(day);
            }
        }

        printer.printTable(headers, data);

        if (selectableDays.isEmpty()) {
            printer.printInfo("Non ci sono sessioni disponibili da avviare.");
            reader.waitForEnter();
            return this;
        }

        printer.printMenu("Seleziona una sessione da visualizzare o avviare",
                selectableDays.stream().map(d -> "Giorno " + d.getAbsoluteDay() + " - " + d.getSession().getName()).toList());
        int scelta = reader.readInt("Scegli un'opzione (-1 per tornare indietro): ", -1, selectableDays.size());

        if (scelta == -1) {
            return this;
        }

        return sessionPrinter(selectableDays.get(scelta), schedule.getPlanId());
    }

    private CliView sessionPrinter(ScheduleDayBean selectedDay, String planId) {
        if(selectedDay == null) {
            printer.printInfo("Sessione non trovata.");
            return this;
        }

        WorkoutSessionBean session = selectedDay.getSession();
        if (session == null) return this;

        printer.printPlan(session.getPlanRoot());
        reader.waitForEnter();

        printer.printMenu("Vuoi avviare la sessione?", List.of("Avvia", BACK_LABEL));
        int scelta = reader.readInt(CHOOSE_OPTION_PROMPT, 1, 2);
        if (scelta == 1) {
            return new WorkoutCli(planId, selectedDay.getAbsoluteDay());
        }

        return this;
    }

    private CliView todayPlan() {
            WorkoutScheduleBean day = planManager.getCurrentCycleScheduleAsync()
                    .exceptionally(e -> {
                        printer.printException("Errore nel caricamento del piano di allenamento: ", e);
                        return null;
                    }).join();

            int suggestedDay = day.getSuggestedDayIndex();

            WorkoutSessionBean todayPlan = day.getDays().get(suggestedDay).getSession();
            if (todayPlan == null || todayPlan.getPlanRoot() == null) {
                printer.printInfo("Non hai ancora pianificato un giorno di allenamento.");
                return this;
            }

            printer.printPlan(todayPlan.getPlanRoot());
            reader.waitForEnter();

            printer.printMenu("Vuoi avviare la scheda del giorno corrente?", List.of("Avvia", BACK_LABEL));
            int scelta = reader.readInt(CHOOSE_OPTION_PROMPT, 1, 2);

            if (scelta == 1) {
                return new WorkoutCli(day.getPlanId(), day.getDays().get(suggestedDay).getAbsoluteDay());
            }

        return this;
    }
}
