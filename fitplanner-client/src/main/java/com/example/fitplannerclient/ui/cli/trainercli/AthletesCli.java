package com.example.fitplannerclient.ui.cli.trainercli;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.ui.cli.AbstractCliView;
import com.example.fitplannerclient.ui.cli.CliView;
import com.example.fitplannerclient.ui.cli.DashboardCli;
import com.example.fitplannerclient.ui.cli.athletecli.StatisticsCli;

import java.util.List;

public class AthletesCli extends AbstractCliView {

    @Override
    protected CliView render() {
        ProfileManager profileManager = engine.getSessionContext().createProfileManager();

        printer.printHeader("I MIEI ATLETI");

        List<ProfileBean> athletes = profileManager.getMyAthletesAsync()
                .exceptionally(ex -> {
                    printer.printException("Errore nel caricamento degli atleti:", ex);
                    return null;
                }).join();

        if (athletes == null || athletes.isEmpty()) {
            printer.printInfo("Non hai ancora nessun atleta associato.");
            reader.waitForEnter();
            return new DashboardCli();
        }

        return reader.selectFrom("Seleziona un atleta per visualizzarne i progressi:", athletes,
                        a -> a.getFirstName() + " " + a.getLastName() + " (" + a.getContactEmail() + ")", "Indietro")
                .<CliView>map(a -> new StatisticsCli(a.getUserId()))
                .orElseGet(DashboardCli::new);
    }
}
