package com.example.fitplannerclient.ui.cli;

import com.example.fitplannerclient.controller.session.NotificationManager;

import java.util.List;

public class NotificationCli extends AbstractCliView {

    @Override
    protected CliView render() {
        NotificationManager notificationManager = engine.getSessionContext().createNotificationManager();
        try {
            List<String> notifications = notificationManager.getNotifications();

            printer.printHeader("NOTIFICHE");

            if (notifications != null && !notifications.isEmpty()) {
                for (String notification : notifications) {
                    printer.printInfo("- " + notification);
                }
            } else {
                printer.printInfo("Non hai nessuna nuova notifica.");
            }

            reader.waitForEnter();

        } catch (Exception e) {
            printer.printException("Errore nel caricamento delle notifiche: ", e);
            reader.waitForEnter();
        }
        return new DashboardCli();
    }
}
