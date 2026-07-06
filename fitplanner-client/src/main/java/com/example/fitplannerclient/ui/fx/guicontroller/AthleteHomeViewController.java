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

import com.example.fitplannerclient.controller.session.NotificationManager;
import com.example.fitplannerclient.ui.fx.components.FormField;
import com.example.fitplannerclient.util.ValidationUtils;

public class AthleteHomeViewController implements GuiController {
    private final AthleteHomeView view;
    private final HeaderViewController headerViewController;
    private final Navigator navigator;
    private final ProfileManager profileManager;
    private final WorkoutPlanManager planManager;
    private final GuiManager guiManager;

    public AthleteHomeViewController(Navigator navigator, GuiManager guiManager, ProfileManager profileManager, WorkoutPlanManager planManager, NotificationManager notificationManager) {
        this.navigator = navigator;
        this.profileManager = profileManager;
        this.planManager = planManager;
        this.guiManager = guiManager;
        this.headerViewController = new HeaderViewController(navigator, notificationManager, 0, HeaderViewController.Type.ATHLETE); // Active Index 0 is Home
        this.view = new AthleteHomeView(headerViewController.getView());
        bindValidators();
        setupActions();
    }

    private void bindValidators() {
        view.getInviteCodeField().setValidator(code -> ValidationUtils.validateRequired(code, "Codice Invito", 36));
    }

    private void setupActions() {
        view.setOnTrainerInviteSubmitAction(this::handleTrainerInviteSubmit);
    }

    private void handleTrainerInviteSubmit() {
        FormField codeField = view.getInviteCodeField();
        if (!codeField.validate()) return;
        
        String code = codeField.getText().trim();
        profileManager.linkTrainerAsync(code).thenRun(() ->
            Platform.runLater(() -> {
                guiManager.showNotification(GuiManager.NotificationType.SUCCESS, "Trainer collegato con successo!");
                start(); // Refresh home view
            })
        ).exceptionally(e -> {
            guiManager.showExceptionError("Errore:", e);
            return null;
        });
    }

    @Override
    public Pane getView() {
        return this.view;
    }

    @Override
    public void start() {
        headerViewController.start();
        ProfileBean profile = profileManager.getCacheProfileInfo();
        view.setWelcomeMessage("Benvenuto, " + profile.getFirstName(), "Supera i tuoi limiti oggi!");

        // Load the current cycle schedule
        planManager.getCurrentCycleScheduleAsync()
                .thenAccept(schedule -> Platform.runLater(() -> {
                    if (schedule == null) {
                        view.showNoPlanAssigned();
                    } else {
                        view.setOnStartSessionAction(absoluteDay ->
                                navigator.goToWorkoutExecution(schedule.getPlanId(), absoluteDay)
                        );
                        view.showAthleteDashboard(schedule);
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
                Platform.runLater(view::showTrainerInviteCard);
            }
        }).exceptionally(e -> {
            guiManager.showExceptionError("Errore durante il controllo del trainer:", e);
            return null;
        });
    }

    @Override
    public void stop() {
        headerViewController.stop();
    }
}
