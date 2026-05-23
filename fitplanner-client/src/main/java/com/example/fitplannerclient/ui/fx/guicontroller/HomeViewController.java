package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.HomeView;
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
        ProfileBean profile = profileManager.getCachedProfile();
        boolean isTrainer = profile.getProfileType() == ProfileBean.ProfileType.TRAINER;
        
        String welcomeTitle = "Benvenuto, " + profile.getFirstName() + "!";
        String welcomeSubtitle = isTrainer 
                ? "Gestisci la tua libreria e i piani dei tuoi atleti." 
                : "Supera i tuoi limiti oggi!";
        view.setWelcomeMessage(welcomeTitle, welcomeSubtitle);

        if (isTrainer) {
            view.showTrainerDashboard(
                    () -> Navigator.getInstance().goToExerciseLibrary(),
                    () -> Navigator.getInstance().goToWorkoutPlanEditor()
            );
        } else {
            // Load athlete plan and suggested session
            planManager.getAssignedPlanAsync()
                    .thenCombine(planManager.getNextSuggestedSessionAsync(), (plan, session) -> {
                        Platform.runLater(() -> {
                            view.showAthleteDashboard(plan, session, () -> {
                                Navigator.getInstance().goToWorkoutExecution(session);
                            });
                        });
                        return null;
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> view.showNoPlanAssigned());
                        return null;
                    });
        }
    }

    @Override
    public void stop() {}
}
