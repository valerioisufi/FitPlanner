package com.example.fitplannerclient.ui.cli;

import java.util.List;

public class DashboardCli implements CliView {

    @Override
    public CliView execute(CliEngine engine) {
        engine.getPrinter().printHeader("DASHBOARD");
        engine.getPrinter().printMenu(null, List.of("Profilo", "", "Esci"));

        int scelta = engine.getInput().readInt("Scegli un'opzione: ", 1, 3);

        if (scelta == 1) {
            return new ProfileCli();
        } else if (scelta == 2) {
            // Temporaneamente vuoto
        } else if (scelta == 3) {
            return null;
        }

        return this;
    }

    @Override
    public void stop() { 
        // Intenzionalmente vuoto
    }
}