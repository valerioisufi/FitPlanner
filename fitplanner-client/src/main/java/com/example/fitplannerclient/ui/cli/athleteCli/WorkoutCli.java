package com.example.fitplannerclient.ui.cli.athleteCli;

import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.ui.cli.*;
import com.example.fitplannerclient.ui.cli.io.InputReader;
import com.example.fitplannerclient.ui.cli.io.OutputPrinter;

public class WorkoutCli implements CliView {
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


    }

    @Override
    public void stop() {
        // Intenzionalmente vuoto
    }

}
