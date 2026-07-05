package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.controller.session.NotificationManager;
import com.example.fitplannerclient.controller.session.NotificationObserver;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.common.HeaderView;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

import java.util.List;

public class HeaderViewController implements GuiController {
    private final HeaderView headerView;

    private final Navigator navigator;
    private final NotificationManager notificationManager;

    private NotificationObserver notificationObserver;

    public HeaderViewController(Navigator navigator, NotificationManager notificationManager, int activeIndex, Type type) {
        this.navigator = navigator;
        this.notificationManager = notificationManager;

        List<HeaderView.MenuConfig> navItems = type == Type.TRAINER ? getTrainerHeaderItems() : getAthleteHeaderItems();

        headerView = new HeaderView(navItems, activeIndex);

        headerView.setupAccountMenu(
                navigator::goToProfile,
                navigator::logout
        );
    }

    private List<HeaderView.MenuConfig> getAthleteHeaderItems() {
        return List.of(
                new HeaderView.MenuConfig("Home", null, node -> navigator.goHome()),
                new HeaderView.MenuConfig("Il Mio Piano", null, node -> {}),
                new HeaderView.MenuConfig("Statistiche", null, node -> navigator.goToStatistics())
        );
    }

    private List<HeaderView.MenuConfig> getTrainerHeaderItems() {
        return List.of(
                new HeaderView.MenuConfig("Home", null, node -> navigator.goHome()),
                new HeaderView.MenuConfig("Libreria Esercizi", null, node -> navigator.goToExerciseLibrary()),
                new HeaderView.MenuConfig("Gestione Piani", null, node -> navigator.goToPlanManagement())
        );
    }

    @Override
    public Pane getView() {
        return headerView;
    }

    @Override
    public void start() {
        notificationObserver = () ->
            // Update the notification menu with the new message
            Platform.runLater(() -> headerView.updateNotifications(notificationManager.getNotifications()));

        notificationManager.attachObserver(notificationObserver);
        notificationObserver.onNotificationReceived();
    }

    @Override
    public void stop() {
        notificationManager.detachObserver(notificationObserver);
    }

    public enum Type {
        ATHLETE, TRAINER
    }
}