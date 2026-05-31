package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.dashboard.HomeView;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

public class HomeViewController implements GuiController {
    private final HomeView view;
    private final HeaderViewController headerViewController;
    private final ProfileManager profileManager;
    private final WorkoutPlanManager planManager;

    public HomeViewController(ProfileManager profileManager, WorkoutPlanManager planManager) {
        this.profileManager = profileManager;
        this.planManager = planManager;
        this.headerViewController = new HeaderViewController(0, profileManager); // Active Index 0 is Home
        this.view = new HomeView(headerViewController.getView());
    }

    @Override
    public Pane getView() {
        return this.view;
    }

    @Override
    public void start() {
        ProfileBean profile = profileManager.getCacheProfileInfo();
        boolean isTrainer = profile.getProfileType() == ProfileBean.ProfileType.TRAINER;
        
        String welcomeTitle = "Benvenuto, " + profile.getFirstName() + "!";
        String welcomeSubtitle = isTrainer 
                ? "Gestisci la tua libreria e i piani dei tuoi atleti." 
                : "Supera i tuoi limiti oggi!";
        view.setWelcomeMessage(welcomeTitle, welcomeSubtitle);

        if (isTrainer) {
            view.showTrainerDashboard(
                    () -> Navigator.getInstance().goToExerciseLibrary(),
                    () -> Navigator.getInstance().goToPlanManagement()
            );

            // Fetch and set invite code
            profileManager.getInvitationCodeAsync()
                    .thenAccept(code -> javafx.application.Platform.runLater(() -> view.setInviteCode(code)))
                    .exceptionally(ex -> null);

            // Fetch and set athletes
            profileManager.getMyAthletesAsync()
                    .thenAccept(athletes -> javafx.application.Platform.runLater(() -> 
                            view.showAthleteList(athletes, athlete -> Navigator.getInstance().goToAthleteDashboard(athlete))
                    ))
                    .exceptionally(ex -> {
                        Navigator.getInstance().getGuiManager().showExceptionError(
                            "Errore nel caricamento degli atleti:", ex);
                        return null;
                    });
        } else {
            // Load athlete plan and suggested session
            planManager.getAssignedPlanAsync()
                    .thenCombine(planManager.getNextSuggestedSessionAsync(), (plan, session) -> {
                        Platform.runLater(() -> {
                            view.showAthleteDashboard(plan, session, () -> {
                                Navigator.getInstance().goToWorkoutExecution(session);
                            });
                            checkAndShowTrainerInvite();
                        });
                        return null;
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            view.showNoPlanAssigned();
                            checkAndShowTrainerInvite();
                        });
                        return null;
                    });
        }
    }

    private void checkAndShowTrainerInvite() {
        profileManager.getMyTrainerAsync().thenAccept(trainer -> {
            if (trainer == null) {
                Platform.runLater(() -> view.showTrainerInviteCard(code -> {
                    profileManager.linkTrainerAsync(code).thenRun(() -> {
                        Platform.runLater(() -> {
                            Navigator.getInstance().getGuiManager().showNotification(GuiManager.NotificationType.SUCCESS, "Trainer collegato con successo!");
                            start(); // Refresh home view
                        });
                    }).exceptionally(e -> {
                        Navigator.getInstance().getGuiManager().showExceptionError("Codice invito non valido o errore di rete:", e);
                        return null;
                    });
                }));
            }
        }).exceptionally(e -> null);
    }

    @Override
    public void stop() {}
}
