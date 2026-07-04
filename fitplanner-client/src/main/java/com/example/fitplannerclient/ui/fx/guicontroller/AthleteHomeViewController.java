package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.dashboard.AthleteHomeView;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

public class AthleteHomeViewController implements GuiController {
    private final AthleteHomeView view;
    private final HeaderViewController headerViewController;
    private final Navigator navigator;
    private final ProfileManager profileManager;
    private final WorkoutPlanManager planManager;
    private final GuiManager guiManager;

    public AthleteHomeViewController(Navigator navigator, ProfileManager profileManager, WorkoutPlanManager planManager, GuiManager guiManager) {
        this.navigator = navigator;
        this.profileManager = profileManager;
        this.planManager = planManager;
        this.guiManager = guiManager;
        this.headerViewController = new HeaderViewController(navigator, 0, profileManager); // Active Index 0 is Home
        this.view = new AthleteHomeView(headerViewController.getView());
    }

    @Override
    public Pane getView() {
        return this.view;
    }

    @Override
    public void start() {
        ProfileBean profile = profileManager.getCacheProfileInfo();
        view.setWelcomeMessage("Benvenuto, " + profile.getFirstName(), "Supera i tuoi limiti oggi!");

        // Load the current cycle schedule
        planManager.getCurrentCycleScheduleAsync()
                .thenAccept(schedule -> Platform.runLater(() -> {
                    if (schedule == null || schedule.getSuggestedDayIndex() < 0) {
                        view.showNoPlanAssigned();
                    } else {
                        view.showAthleteDashboard(schedule, absoluteDay ->
                                navigator.goToWorkoutExecution(schedule.getPlanId(), absoluteDay)
                        );
                    }
                    checkAndShowTrainerInvite();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        view.showNoPlanAssigned();
                        checkAndShowTrainerInvite();
                    });
                    return null;
                });
    }

    private void checkAndShowTrainerInvite() {
        profileManager.hasTrainerAsync().thenAccept(hasTrainer -> {
            if (Boolean.FALSE.equals(hasTrainer)) {
                Platform.runLater(() -> view.showTrainerInviteCard(code ->
                    profileManager.linkTrainerAsync(code).thenRun(() ->
                        Platform.runLater(() -> {
                            guiManager.showNotification(GuiManager.NotificationType.SUCCESS, "Trainer collegato con successo!");
                            start(); // Refresh home view
                        })

                    ).exceptionally(e -> {
                        guiManager.showExceptionError("Errore:", e);
                        return null;
                    })
                ));
            }

        }).exceptionally(e -> {
            guiManager.showExceptionError("Errore durante il controllo del trainer:", e);
            return null;
        });
    }

    @Override
    public void stop() {
        // Metodo intenzionalmente vuoto
    }
}
