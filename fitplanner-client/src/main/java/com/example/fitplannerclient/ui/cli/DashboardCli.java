package com.example.fitplannerclient.ui.cli;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.ui.cli.athleteCli.PlansCli;
import com.example.fitplannerclient.ui.cli.athleteCli.WorkoutCli;

import java.util.List;

public class DashboardCli implements CliView {


    @Override
    public CliView execute(CliEngine engine) {
        engine.getPrinter().printHeader("DASHBOARD");
        ProfileBean.ProfileType type = engine.getSessionContext().createProfileManager().getProfileInfoAsync().join().getProfileType();
        String msg;
        if (type == ProfileBean.ProfileType.ATHLETE) {
            athleteDashboard(engine);
        } else {
            trainerDashboard(engine);
        }
        return this;
    }

    private CliView athleteDashboard(CliEngine engine){
        engine.getPrinter().printMenu(null, List.of("Profilo", "Notifiche", "Il piano di oggi", "I miei piani", "statistiche", "Esci"));
        int scelta = engine.getInput().readInt("Scegli un'opzione: ", 1, 6);

        switch (scelta){
            case 1: return new ProfileCli();
            case 2: return new NotificationCli();
            case 3: todayPlan();
            case 4: return new WorkoutCli();
            case 5: return new PlansCli();
            case 6: return new StatisticsCli();
            case 7: return null;
        }
        return this;
    }

    private void todayPlan() {
    }

    private CliView trainerDashboard(CliEngine engine){
        return this;
    }

    @Override
    public void stop() { 
        // Intenzionalmente vuoto
    }
}