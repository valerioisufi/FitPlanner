package com.example.fitplannerclient.ui.cli;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.ui.cli.athletecli.SessionsCli;
import com.example.fitplannerclient.ui.cli.athletecli.StatisticsCli;
import com.example.fitplannerclient.ui.cli.trainercli.AthletesCli;
import com.example.fitplannerclient.ui.cli.trainercli.ExerciseLibraryCli;
import com.example.fitplannerclient.ui.cli.trainercli.WorkoutPlanLibraryCli;

import java.util.List;

public class DashboardCli extends AbstractCliView {

    @Override
    protected CliView render() {
        printer.printHeader("DASHBOARD");
        ProfileBean.ProfileType type = engine.getProfileType();
        return type == ProfileBean.ProfileType.ATHLETE ? athleteDashboard() : trainerDashboard();
    }

    private CliView athleteDashboard() {
        printer.printMenu(null, List.of(
                "Sessioni di allenamento",
                "Statistiche",
                "Profilo",
                "Notifiche",
                "Esci"
        ));
        int scelta = reader.readInt("Scegli un'opzione: ", 1, 5);
        return switch (scelta) {
            case 1 -> new SessionsCli();
            case 2 -> new StatisticsCli();
            case 3 -> new ProfileCli();
            case 4 -> new NotificationCli();
            case 5 -> null;
            default -> this;
        };
    }

    private CliView trainerDashboard() {
        printer.printMenu(null, List.of(
                "I miei Atleti",
                "Libreria esercizi",
                "Piani di allenamento",
                "Profilo",
                "Notifiche",
                "Esci"
        ));
        int scelta = reader.readInt("Scegli un'opzione: ", 1, 6);
        return switch (scelta) {
            case 1 -> new AthletesCli();
            case 2 -> new ExerciseLibraryCli();
            case 3 -> new WorkoutPlanLibraryCli();
            case 4 -> new ProfileCli();
            case 5 -> new NotificationCli();
            case 6 -> null;
            default -> this;
        };
    }
}
