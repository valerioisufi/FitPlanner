package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.controller.plan.EditWorkoutPlanManager;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.controller.plan.observer.WorkoutPlanObserver;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.WorkoutPlanEditorView;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class WorkoutPlanEditorViewController implements GuiController {

    private final BorderPane mainPane;
    private final WorkoutPlanEditorView view;

    private WorkoutPlanBean activePlan;
    private WorkoutSessionBean activeSession;
    private final EditWorkoutPlanManager editWorkoutPlanManager;
    private final ExerciseLibraryManager exerciseManager;

    private final WorkoutPlanObserver observer;

    public WorkoutPlanEditorViewController(String planIdToEdit, boolean copyOfExisting, EditWorkoutPlanManager editWorkoutPlanManager, ExerciseLibraryManager exerciseManager) {
        this.editWorkoutPlanManager = editWorkoutPlanManager;
        this.exerciseManager = exerciseManager;

        this.view = new WorkoutPlanEditorView();
        this.mainPane = new BorderPane();
        this.mainPane.setCenter(this.view);

        observer = () -> Platform.runLater(() -> {
            activePlan = editWorkoutPlanManager.getPlan();
            view.setPlan(activePlan);
        });
        editWorkoutPlanManager.addObserver(observer);

        if (planIdToEdit == null) {
            this.editWorkoutPlanManager.createNewPlan()
                    .exceptionally(ex -> {
                        Platform.runLater(() -> Navigator.getInstance().getGuiManager().showNotification(
                                GuiManager.NotificationType.ERROR,
                                "Errore nella creazione del piano: " + ex.getMessage()));
                        return null;
                    });
        } else {
            this.editWorkoutPlanManager.editExistingPlan(planIdToEdit, copyOfExisting)
                    .exceptionally(ex -> {
                        Platform.runLater(() -> Navigator.getInstance().getGuiManager().showNotification(
                                GuiManager.NotificationType.ERROR,
                                "Errore nel caricamento del piano: " + ex.getMessage()));
                        return null;
                    });
        }

        // Bind callbacks
        this.view.setOnSessionSelected(session -> this.activeSession = session);
        this.view.setOnManageSessionsRequested(() -> {
            if (activePlan != null) {
                this.view.showManageSessionsModal(
                    activePlan.getCycleLength(),
                    activePlan.getSessions(),
                    (newCycleLength, updatedSessions) -> {
                        activePlan.setCycleLength(newCycleLength);
                        activePlan.setSessions(updatedSessions);
                        this.view.setPlan(activePlan);
                        if (!updatedSessions.contains(activeSession)) {
                            this.activeSession = updatedSessions.isEmpty() ? null : updatedSessions.getFirst();
                        }
                    }
                );
            }
        });

        this.view.setOnSavePlanClicked(this::savePlan);
        this.view.setOnCancelClicked(() -> Navigator.getInstance().goToPlanManagement());

        this.view.setOnShowModalRequested(modalContent -> Navigator.getInstance().getGuiManager().showModal(modalContent));
        this.view.setOnHideModalRequested(() -> Navigator.getInstance().getGuiManager().hideModal());

        this.view.disableEditing(false);
    }

    private void loadLibrary() {
        exerciseManager.getExercisesAsync(null)
                .thenAccept(exercises -> Platform.runLater(() -> view.setExercises(exercises)))
                .exceptionally(ex -> {
                    System.err.println("Errore (rimosso): " + ex.getMessage());
                    return null;
                });
    }



    private void savePlan() {
        if (activePlan == null) return;


    }

    @Override
    public Pane getView() {
        return this.mainPane;
    }

    @Override
    public void start() {
        this.view.setPlan(this.activePlan);

        if (activePlan != null && activePlan.getSessions() != null && !activePlan.getSessions().isEmpty()) {
            this.activeSession = activePlan.getSessions().getFirst();
        }

        loadLibrary();
    }

    @Override
    public void stop() {
        editWorkoutPlanManager.removeObserver(observer);
        Navigator.getInstance().getGuiManager().hideModal();
    }

}
