package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.dashboard.AthleteHomeView;
import com.example.fitplannerclient.ui.fx.view.dashboard.TrainerHomeView;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

public class HomeViewController implements GuiController {
    private Pane view;
    private final HeaderViewController headerViewController;
    private final Navigator navigator;
    private final ProfileManager profileManager;
    private final WorkoutPlanManager planManager;
    private final GuiManager guiManager;

    public HomeViewController(Navigator navigator, ProfileManager profileManager, WorkoutPlanManager planManager, GuiManager guiManager) {
        this.navigator = navigator;
        this.profileManager = profileManager;
        this.planManager = planManager;
        this.guiManager = guiManager;
        this.headerViewController = new HeaderViewController(navigator, 0, profileManager); // Active Index 0 is Home
        
        ProfileBean profile = profileManager.getCacheProfileInfo();
        boolean isTrainer = profile != null && profile.getProfileType() == ProfileBean.ProfileType.TRAINER;
        
        if (isTrainer) {
            this.view = new TrainerHomeView(headerViewController.getView());
        } else {
            this.view = new AthleteHomeView(headerViewController.getView());
        }
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

        if (isTrainer) {
            TrainerHomeView trainerView = (TrainerHomeView) this.view;
            trainerView.setWelcomeMessage(welcomeTitle, welcomeSubtitle);
            trainerView.showTrainerDashboard(
                    navigator::goToExerciseLibrary,
                    navigator::goToPlanManagement
            );

            // Fetch and set invite code
            profileManager.getInvitationCodeAsync()
                    .thenAccept(code -> Platform.runLater(() -> trainerView.setInviteCode(code)))
                    .exceptionally(ex -> {
                        guiManager.showExceptionError("Errore nel recupero del codice di invito:", ex);
                        return null;
                    });

            // Fetch and set athletes
            profileManager.getMyAthletesAsync()
                    .thenAccept(athletes -> Platform.runLater(() ->
                            trainerView.showAthleteList(athletes, navigator::goToAthleteDashboard)
                    ))
                    .exceptionally(ex -> {
                        guiManager.showExceptionError(
                            "Errore nel caricamento degli atleti:", ex);
                        return null;
                    });
        } else {
            AthleteHomeView athleteView = (AthleteHomeView) this.view;
            athleteView.setWelcomeMessage(welcomeTitle, welcomeSubtitle);
            
            // Load athlete plan and suggested session
            planManager.getAssignedPlanAsync()
                    .thenCombine(planManager.getCurrentCycleScheduleAsync(), (plan, schedule) -> {
                        Platform.runLater(() -> {
                            athleteView.showAthleteDashboard(plan, schedule, selectedRelativeDay -> {
                                if (schedule != null) {
                                    navigator.goToWorkoutExecution(schedule.getPlanId(), selectedRelativeDay);
                                }
                            });
                            checkAndShowTrainerInvite(athleteView);
                        });
                        return null;
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            athleteView.showNoPlanAssigned();
                            checkAndShowTrainerInvite(athleteView);
                        });
                        return null;
                    });
        }
    }

    private void checkAndShowTrainerInvite(AthleteHomeView athleteView) {
        profileManager.hasTrainerAsync().thenAccept(hasTrainer -> {
            if (Boolean.FALSE.equals(hasTrainer)) {
                Platform.runLater(() -> athleteView.showTrainerInviteCard(code ->
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
            Platform.runLater(() -> guiManager.showExceptionError("Errore durante il controllo del trainer:", e));
            return null;
        });
    }

    @Override
    public void stop() {
        // Metodo intenzionalmente vuoto
    }
}
