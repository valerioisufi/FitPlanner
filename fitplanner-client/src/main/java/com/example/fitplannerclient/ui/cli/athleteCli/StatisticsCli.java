package com.example.fitplannerclient.ui.cli.athleteCli;

import com.example.fitplannerclient.ui.cli.CliEngine;
import com.example.fitplannerclient.ui.cli.CliView;
import com.example.fitplannerclient.ui.cli.DashboardCli;

public class StatisticsCli implements CliView {
    @Override
    public CliView execute(CliEngine engine) {
        engine.getPrinter().printHeader("STATISTICHE");
        engine.getPrinter().printInfo("Funzionalità non ancora implementata.");
        engine.getInput().waitForEnter();
        return new DashboardCli();
    }

    @Override
    public void stop() {
        // Intenzionalmente vuoto
    }

}
