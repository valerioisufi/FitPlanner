package com.example.fitplannerclient.ui.cli;

import com.example.fitplannerclient.context.UserSessionContext;
import com.example.fitplannerclient.controller.SessionController;

import java.util.Scanner;

public class CliEngine {
    private final SessionController sessionController;

    private CliView currentCliView;
    private final InputReader inputReader;
    private final OutputPrinter outputPrinter;

    public CliEngine(SessionController sessionController) {
        this.sessionController = sessionController;

        Scanner scanner = new Scanner(System.in);
        this.outputPrinter = new OutputPrinter();
        this.inputReader = new InputReader(scanner, outputPrinter);
    }

    public SessionController getSessionController() { return sessionController; }
    public UserSessionContext getSessionContext() { return sessionController.getSession(); }

    public InputReader getInput() { return inputReader; }
    public OutputPrinter getPrinter() { return outputPrinter; }

    public void start() {
        currentCliView = initialView();

        while (currentCliView != null) {

            CliView nextView = currentCliView.execute(this);

            // sessione non più valida (refresh token scaduto): si torna al login
            if (nextView != null && !(nextView instanceof AuthenticationCli) && !sessionController.isAuthenticated()) {
                currentCliView.stop();
                currentCliView = new AuthenticationCli();
                continue;
            }

            if (currentCliView != nextView) {
                currentCliView.stop();
            }

            currentCliView = nextView;
        }
        outputPrinter.printInfo("Chiusura dell'applicazione.");
    }

    private CliView initialView() {
        if (sessionController.hasPersistedTokens()) {
            try {
                sessionController.resumeSessionAsync().join();
                return new DashboardCli();

            } catch (Exception e) {
                outputPrinter.printInfo("Sessione scaduta, effettua di nuovo il login.");
            }
        }

        return new AuthenticationCli();
    }

}
