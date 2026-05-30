package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.plan.management.PlanManagementView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class PlanManagementViewController implements GuiController {

    private final PlanManagementView view;
    private final HeaderViewController headerViewController;

    private final WorkoutPlanManager planManager;
    private final ProfileManager profileManager;
    private final GuiManager guiManager;

    private List<ProfileBean> athletesCache = new ArrayList<>();

    public PlanManagementViewController(WorkoutPlanManager planManager, ProfileManager profileManager, GuiManager guiManager) {
        this.planManager = planManager;
        this.profileManager = profileManager;
        this.guiManager = guiManager;

        this.headerViewController = new HeaderViewController(2, profileManager);
        this.view = new PlanManagementView();
        this.view.setHeaderView(this.headerViewController.getView());

        bindActions();
    }

    private void bindActions() {
        view.setOnNewPlanAction(() -> {
            Navigator.getInstance().goToWorkoutPlanEditor(null, false);
        });

        view.setOnEditAction(plan -> {
            Navigator.getInstance().goToWorkoutPlanEditor(plan.getPlanId(), false);
        });

        view.setOnDeleteAction(plan -> {
            planManager.deletePlanAsync(plan.getPlanId())
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        guiManager.showNotification(GuiManager.NotificationType.SUCCESS, "Piano eliminato con successo");
                        loadPlans();
                    });
                })
                .exceptionally(ex -> {
                    guiManager.showExceptionError("Errore nell'eliminazione:", ex);
                    return null;
                });
        });

        view.setOnCloneAction(plan -> {
            Navigator.getInstance().goToWorkoutPlanEditor(plan.getPlanId(), true);
        });

        // Assign Button logic
        view.setOnAssignButtonClick(plan -> {
            // TODO
//            if (athletesCache.isEmpty()) {
//                // Fetch athletes if not already cached
//                profileManager.getMyAthletesAsync().thenAccept(athletes -> {
//                    athletesCache = athletes;
//                    Platform.runLater(() -> view.showModal(plan, athletes));
//                }).exceptionally(ex -> {
//                    Platform.runLater(() -> guiManager.showNotification(GuiManager.NotificationType.ERROR, "Impossibile recuperare gli atleti"));
//                    return null;
//                });
//            } else {
//                view.showModal(plan, athletesCache);
//            }
        });

        // Modal close/assign logic
        view.getAssignModal().setOnCloseAction(guiManager::hideModal);
        
        view.getAssignModal().setOnAssignAction(athlete -> {
            WorkoutPlanBean planToAssign = view.getAssignModal().getCurrentPlan();
            if (planToAssign != null) {
                planManager.assignPlanToAthleteAsync(planToAssign.getId(), athlete.getUserId())
                    .thenRun(() -> {
                        Platform.runLater(() -> {
                            guiManager.hideModal();
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Piano Assegnato");
                            alert.setHeaderText(null);
                            alert.setContentText("Il piano \"" + planToAssign.getName() + "\" è stato assegnato a " + athlete.getFirstName() + " " + athlete.getLastName());
                            alert.showAndWait();
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            guiManager.hideModal();
                            guiManager.showExceptionError("Errore nell'assegnazione:", ex);
                        });
                        return null;
                    });
            }
        });
    }

    @Override
    public void start() {
        loadPlans();
        
        // Pre-fetch athletes for faster modal open
        profileManager.getMyAthletesAsync().thenAccept(athletes -> athletesCache = athletes).exceptionally(ex -> null);
    }

    private void loadPlans() {
        planManager.getMyCreatedPlansSummaryAsync()
            .thenAccept(plans -> Platform.runLater(() -> view.setPlansList(plans)))
            .exceptionally(ex -> {
                guiManager.showExceptionError("Errore nel caricamento dei piani:", ex);
                return null;
            });
    }

    @Override
    public void stop() {
        guiManager.hideModal();
    }

    @Override
    public Pane getView() {
        return this.view;
    }
}
