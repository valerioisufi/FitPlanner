package com.example.fitplannerclient.ui.cli;

import com.example.fitplannerclient.bean.plan.ExerciseModifierBean;
import com.example.fitplannerclient.bean.plan.WorkoutSessionBean;
import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.ui.cli.athleteCli.SessionsCli;
import com.example.fitplannerclient.ui.cli.athleteCli.StatisticsCli;
import com.example.fitplannerclient.ui.cli.athleteCli.WorkoutCli;
import com.example.fitplannerclient.ui.cli.io.InputReader;
import com.example.fitplannerclient.ui.cli.io.OutputPrinter;

import java.util.List;

public class DashboardCli implements CliView {
    CliEngine engine;
    OutputPrinter printer;
    InputReader reader;
    WorkoutPlanManager planManager;

    @Override
    public CliView execute(CliEngine engine) {
        this.engine = engine;
        this.printer = engine.getPrinter();
        this.reader = engine.getInput();
        this.planManager = engine.getSessionContext().createWorkoutPlanManager(); // evitiamo i nullPointer inizializzando

        engine.getPrinter().printHeader("DASHBOARD");
        ProfileBean.ProfileType type = engine.getSessionContext().createProfileManager().getProfileInfoAsync().join().getProfileType();

        if (type == ProfileBean.ProfileType.ATHLETE) {
            return athleteDashboard(engine);
        } else {
            return trainerDashboard(engine);
        }
    }

    private CliView athleteDashboard(CliEngine engine){
        engine.getPrinter().printMenu(null, List.of("Profilo", "Notifiche", "La sessione di oggi",
                "Inizia allenamento", "Le mie sessioni di allenamento", "statistiche", "Esci"));
        int scelta = engine.getInput().readInt("Scegli un'opzione: ", 1, 7);

        switch (scelta){
            case 1: return new ProfileCli();
            case 2: return new NotificationCli();
            case 3: {
                todayPlan();
                break;
            }
            case 4: return new WorkoutCli();
            case 5: return new SessionsCli();
            case 6: return new StatisticsCli();
            case 7: return null;
        }
        return this;
    }

    public void todayPlan() {
        try{
            var day = planManager.getCurrentCycleScheduleAsync().join();
            int suggestedDay = day.getSuggestedDayIndex();

            var plan = planManager.getAssignedPlanAsync().join();
            WorkoutSessionBean todayPlan = plan.getSession(suggestedDay);
            if(todayPlan == null) return;

            List<ExerciseModifierBean> exercises = todayPlan.getPlanRoot().getModifiers();

            String[] title = {"nome" , todayPlan.getName()};
            printer.printTable(title, null);
            for (ExerciseModifierBean exercise : exercises){
                String[][] data = {
                        {"Esercizio",   exercise.getName()},
                        {"Ripetizioni", exercise.getValue()}
                };
                printer.printTable(null, data);
            }
        }catch (Exception e){
            printer.printException("Errore nel caricamento della scheda del giorno corrente: ", e);
        }
        reader.waitForEnter();
    }

    private CliView trainerDashboard(CliEngine engine){
        return this;
    }

    @Override
    public void stop() { 
        // Intenzionalmente vuoto
    }
}