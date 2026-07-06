package com.example.fitplannerclient.ui.cli.athleteCli;

import com.example.fitplannerclient.bean.plan.ExerciseModifierBean;
import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.bean.plan.WorkoutSessionBean;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.ui.cli.CliEngine;
import com.example.fitplannerclient.ui.cli.CliView;
import com.example.fitplannerclient.ui.cli.DashboardCli;
import com.example.fitplannerclient.ui.cli.io.InputReader;
import com.example.fitplannerclient.ui.cli.io.OutputPrinter;

import java.util.List;

public class SessionsCli implements CliView {
    CliEngine engine;
    OutputPrinter printer;
    InputReader reader;
    WorkoutPlanManager planManager;
    
    @Override
    public CliView execute(CliEngine engine) {
        this.engine = engine;
        this.printer = engine.getPrinter();
        this.reader = engine.getInput();
        this.planManager = engine.getSessionContext().createWorkoutPlanManager();


        engine.getPrinter().printHeader("LE MIE SESSIONI");
        printer.printMenu(null, List.of("Indietro", "Visualizza le mie sessioni di allenamento"));
        int scelta = engine.getInput().readInt("Scegli un'opzione: ", 1, 2);
        switch (scelta) {
            case 1:
                return new DashboardCli();
            case 2: {
                allSessions(engine);
            }
        }
        return this;
    }


    private void allSessions(CliEngine engine){
        try {
            WorkoutPlanBean plan = planManager.getAssignedPlanAsync().get();
            List<WorkoutSessionBean> sessions = plan.getSessions();

            if (sessions != null && !sessions.isEmpty()) {

                printer.printInfo("Ecco le tue sessioni di allenamento:");
                String[] header = {"nome", "giorno"};
                printer.printTable(header, null);
                for (WorkoutSessionBean session : sessions) {
                    String[] data = {
                            session.getName(),
                            String.valueOf(session.getDay())
                    };
                    printer.printTable(data, null);
                }
                printer.printMenu("Vuoi accedere ad una sessione?", List.of("Si", "Indietro"));
                int scelta = reader.readInt("Scegli un'opzione: ", 1, 2);
                if (scelta == 1) {
                    sessionPrinter(sessions, sessions.size());
                }
                else {
                    return;
                }
            } else {
                printer.printInfo("Non hai ancora nessun piano di allenamento assegnata.");
            }
        } catch (Exception e) {
            printer.printException("Errore nel caricamento del piano di allenamento: ", e);
        }

        engine.getInput().waitForEnter();
    }

    private void sessionPrinter(List<WorkoutSessionBean> sessions, int size){
        printer.printMenu("Seleziona la sessione che vuoi visualizzare", sessions.stream().map(WorkoutSessionBean::getName).toList());
        int scelta = reader.readInt(null, 1, size);
        WorkoutSessionBean session = sessions.get(scelta-1);
        if(session == null) return;

        List<ExerciseModifierBean> exercises = session.getPlanRoot().getModifiers();

        String[] title = {"nome" , session.getName()};
        printer.printTable(title, null);
        for (ExerciseModifierBean exercise : exercises){
            String[][] data = {
                    {"Esercizio",   exercise.getName()},
                    {"Ripetizioni", exercise.getValue()}
            };
            printer.printTable(null, data);
        }
    }

    @Override
    public void stop() {
        // Intenzionalmente vuoto
    }

}
