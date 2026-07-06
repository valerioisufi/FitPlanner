package com.example.fitplannerclient.ui.cli;

import com.example.fitplannerclient.controller.session.NotificationManager;
import com.example.fitplannerclient.ui.cli.io.InputReader;
import com.example.fitplannerclient.ui.cli.io.OutputPrinter;

import java.util.List;

public class NotificationCli implements CliView{
    CliEngine engine;
    OutputPrinter printer;
    InputReader reader;
    NotificationManager notificationManager;

    @Override
    public CliView execute(CliEngine engine) {
        this.engine = engine;
        this.printer = engine.getPrinter();
        this.reader = engine.getInput();
        this.notificationManager = engine.getSessionContext().createNotificationManager();
        try {
            List<String> notifications = notificationManager.getNotifications();

            engine.getPrinter().printHeader("NOTIFICHE");

            if (notifications != null && !notifications.isEmpty()) {
                // Stampa tutte le notifiche
                for (String notification : notifications) {
                    printer.printInfo("- " + notification);
                }
            } else {
                // Feedback se non ci sono notifiche
                printer.printInfo("Non hai nessuna nuova notifica.");
            }

            // Attende l'input dell'utente una sola volta, indipendentemente dal risultato
            reader.waitForEnter();

        } catch (Exception e) {
            printer.printException("Errore nel caricamento delle notifiche: ", e);
            reader.waitForEnter(); // Utile anche qui per far leggere l'errore prima di uscire
        }
        return new DashboardCli();
    }

    @Override
    public void stop() {
        // Intenzionalmente vuoto
    }

}
