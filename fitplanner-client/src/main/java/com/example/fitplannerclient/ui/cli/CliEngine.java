package com.example.fitplannerclient.ui.cli;

import com.example.fitplannerclient.context.UserSessionContext;
import com.example.fitplannerclient.controller.session.SessionManager;
import com.example.fitplannerclient.ui.cli.io.InputReader;
import com.example.fitplannerclient.ui.cli.io.OutputPrinter;

import java.util.Scanner;

public class CliEngine {
    private final SessionManager sessionManager;


    private final InputReader inputReader;
    private final OutputPrinter outputPrinter;

    public CliEngine(SessionManager sessionManager) {
        this.sessionManager = sessionManager;

        Scanner scanner = new Scanner(System.in);
        this.outputPrinter = new OutputPrinter();
        this.inputReader = new InputReader(scanner, outputPrinter);
    }

    public SessionManager getSessionManager() { return sessionManager; }
    public UserSessionContext getSessionContext() { return sessionManager.getSession(); }

    public InputReader getInput() { return inputReader; }
    public OutputPrinter getPrinter() { return outputPrinter; }

    public void start() {
        CliView currentCliView = initialView();

        while (currentCliView != null) {

            CliView nextView = currentCliView.execute(this);

            // sessione non più valida (refresh token scaduto): si torna al login
            if (nextView != null && !(nextView instanceof AuthenticationCli) && !sessionManager.isAuthenticated()) {
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
        if (sessionManager.hasPersistedTokens()) {
            try {
                sessionManager.resumeSessionAsync().join();
                return new DashboardCli();

            } catch (Exception e) {
                outputPrinter.printInfo("Sessione scaduta, effettua di nuovo il login.");
            }
        }

        return new AuthenticationCli();
    }

}
