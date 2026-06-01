package com.example.fitplannerclient.ui.cli;

import com.example.fitplannerclient.AppControllerFactory;
import com.example.fitplannerclient.service.SessionManager;

import java.util.Scanner;

public class CliEngine {
    private SessionManager sessionManager;
    private final AppControllerFactory controllerFactory;

    private CliView currentCliView;
    private final InputReader inputReader;
    private final OutputPrinter outputPrinter;

    public CliEngine(AppControllerFactory controllerFactory, SessionManager sessionManager) {
        this.currentCliView = new DashboardCli();
        this.controllerFactory = controllerFactory;
        this.sessionManager = sessionManager;
        
        Scanner scanner = new Scanner(System.in);
        this.outputPrinter = new OutputPrinter();
        this.inputReader = new InputReader(scanner, outputPrinter);
    }

    public AppControllerFactory getControllerFactory() { return controllerFactory; }
    public SessionManager getSessionManager() { return sessionManager; }

    public InputReader getInput() { return inputReader; }
    public OutputPrinter getPrinter() { return outputPrinter; }

    public void start() {
        while (currentCliView != null) {

            CliView nextView = currentCliView.execute(this);

            if(!sessionManager.isLoggedIn()) {
                currentCliView.stop();
                currentCliView = new AuthenticationCli();
                continue;
            }

            if (currentCliView != nextView) {
                currentCliView.stop();
            }

            currentCliView = nextView;
        }
        System.out.println("Chiusura dell'applicazione.");
    }
}