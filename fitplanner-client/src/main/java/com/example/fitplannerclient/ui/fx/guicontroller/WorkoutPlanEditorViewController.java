package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.WorkoutPlanEditorView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanEditorViewController implements GuiController {

    private final BorderPane mainPane;
    private final WorkoutPlanEditorView view;

    private WorkoutPlanBean activePlan;
    private WorkoutSessionBean activeSession;
    private final WorkoutPlanManager planManager;
    private final ExerciseLibraryManager exerciseManager;
    private final ProfileManager profileManager;

    public WorkoutPlanEditorViewController(WorkoutPlanBean planToEdit, WorkoutPlanManager planManager, ExerciseLibraryManager exerciseManager, ProfileManager profileManager) {
        this.activePlan = planToEdit;
        this.planManager = planManager;
        this.exerciseManager = exerciseManager;
        this.profileManager = profileManager;

        ProfileBean profile = profileManager.getCachedProfile();
        boolean isTrainer = profile != null && profile.getProfileType() == ProfileBean.ProfileType.TRAINER;

        this.view = new WorkoutPlanEditorView();

        this.mainPane = new BorderPane();
        this.mainPane.setCenter(this.view);

        // Bind callbacks
        this.view.setOnSessionSelected(session -> this.activeSession = session);
        this.view.setOnAddSessionClicked(this::addSession);
        this.view.setOnSavePlanClicked(this::savePlan);
        this.view.setOnCancelClicked(() -> {
            if (isTrainer) {
                Navigator.getInstance().goToPlanManagement();
            } else {
                Navigator.getInstance().goHome();
            }
        });

        if (!isTrainer) {
            this.view.disableEditing(true);
        } else {
            this.view.disableEditing(false);
        }
    }

    @Override
    public Pane getView() {
        return this.mainPane;
    }

    @Override
    public void start() {
        ProfileBean profile = profileManager.getCachedProfile();
        boolean isTrainer = profile != null && profile.getProfileType() == ProfileBean.ProfileType.TRAINER;

        if (isTrainer) {
            this.view.setPlan(this.activePlan);
            if (activePlan != null && activePlan.getSessions() != null && !activePlan.getSessions().isEmpty()) {
                this.activeSession = activePlan.getSessions().get(0);
            }
            loadLibrary();
        } else {
            loadAthletePlan();
        }
    }

    @Override
    public void stop() {}

    private void loadAthletePlan() {
        planManager.getAssignedPlanAsync()
                .thenAccept(plan -> Platform.runLater(() -> {
                    if (plan != null) {
                        this.activePlan = plan;
                        view.setPlan(plan);
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> Navigator.getInstance().getGuiManager().showNotification(
                            GuiManager.NotificationType.ERROR, 
                            "Errore nel caricamento dei dati: " + ex.getMessage()));
                    return null;
                });
    }

    private void loadLibrary() {
        exerciseManager.getExercisesAsync(null)
                .thenAccept(exercises -> Platform.runLater(() -> view.setExercises(exercises)))
                .exceptionally(ex -> {
                    System.err.println("Errore (rimosso): " + ex.getMessage());
                    return null;
                });
    }



    private void addSession() {
        if (activePlan == null) return;
        int day = activePlan.getSessions().size() + 1;
        String sessionId = java.util.UUID.randomUUID().toString();
        PlanNodeBean rootNode = new PlanNodeBean("root-" + day, "Sessione Giorno " + day, NodeType.BLOCK);
        rootNode.addFlowDecorator(new FlowDecoratorBean(java.util.UUID.randomUUID().toString(), FlowDecoratorType.REST, "90s"));

        WorkoutSessionBean session = new WorkoutSessionBean(String.valueOf(day), day, rootNode);
        activePlan.addSession(session);
        this.view.setPlan(activePlan);
        this.activeSession = session;
    }

    private void savePlan() {
        if (activePlan == null) return;
        planManager.createPlanAsync(activePlan)
                .thenRun(() -> {
                    Platform.runLater(() -> {
                        Navigator.getInstance().getGuiManager().showNotification(
                                GuiManager.NotificationType.SUCCESS, "Piano salvato con successo.");
                        Navigator.getInstance().goToPlanManagement();
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> Navigator.getInstance().getGuiManager().showNotification(
                            GuiManager.NotificationType.ERROR, 
                            "Errore nel salvataggio: " + ex.getMessage()));
                    return null;
                });
    }
}
