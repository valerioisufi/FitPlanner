package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.ui.fx.view.execution.WorkoutExecutionView;
import com.example.fitplannerclient.service.api.SessionLogApi;
import com.example.fitplannerclient.controller.plan.execution.WorkoutExecutionManager;
import com.example.fitplannercommon.ExerciseLogDTO;
import com.example.fitplannercommon.ExerciseSetDTO;
import com.example.fitplannercommon.SessionLogDTO;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import com.example.fitplannerclient.ui.fx.components.Icon;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class WorkoutExecutionViewController implements GuiController {

    private final BorderPane mainPane;
    private final WorkoutExecutionView view;
    private final HeaderViewController headerViewController;

    private final WorkoutSessionBean sessionBean;
    private final List<PlanNodeBean> exerciseNodes = new ArrayList<>();
    private final List<ExerciseLogDTO> exerciseLogs = new ArrayList<>();
    private final List<ExerciseSetDTO> currentExerciseSets = new ArrayList<>();
    private int currentExerciseIndex = 0;
    private int currentSetNum = 1;
    private int totalSetsForExercise = 3;
    private int targetRepsForExercise = 10;
    private final WorkoutExecutionManager manager;
    private final ProfileManager profileManager;
    private final ExerciseLibraryManager exerciseLibraryManager;
    private boolean isPlaying = true;

    public WorkoutExecutionViewController(WorkoutSessionBean session, WorkoutExecutionManager manager, ProfileManager profileManager, ExerciseLibraryManager exerciseLibraryManager) {
        this.sessionBean = session;
        this.manager = manager;
        this.profileManager = profileManager;
        this.exerciseLibraryManager = exerciseLibraryManager;
        this.headerViewController = new HeaderViewController(1, profileManager); // "Piano" highlight
        this.view = new WorkoutExecutionView(headerViewController.getView());

        this.mainPane = new BorderPane();
        this.mainPane.setCenter(this.view);

        setupButtons();
    }

    private void setupButtons() {
        this.view.getBtnSkipPrevious().setOnAction(e -> skipPrevious());
        this.view.getBtnPlayPause().setOnAction(e -> togglePlayPause());
        this.view.getBtnSkipNext().setOnAction(e -> skipNext());
        this.view.getBtnEndWorkout().setOnAction(e -> finishWorkoutSession());
        
        this.view.setOnLogSetAction(this::handleLogSet);
    }

    @Override
    public Pane getView() {
        return this.mainPane;
    }

    @Override
    public void start() {
        manager.startSession("fake-plan-id", sessionBean);
        manager.play();
        collectExerciseNodes(sessionBean.getPlanRoot());

        if (exerciseNodes.isEmpty()) {
            Platform.runLater(() -> Navigator.getInstance().goHome());
        } else {
            loadExercise(0);
        }
    }

    @Override
    public void stop() {}

    private void collectExerciseNodes(PlanNodeBean node) {
        if (node == null) return;
        if (node.getType() == NodeType.EXERCISE) {
            exerciseNodes.add(node);
        }
        if (node.getChildren() != null) {
            for (PlanNodeBean child : node.getChildren()) {
                collectExerciseNodes(child);
            }
        }
    }

    private void loadExercise(int index) {
        this.currentExerciseIndex = index;
        this.currentExerciseSets.clear();
        this.currentSetNum = 1;
        
        PlanNodeBean exNode = exerciseNodes.get(index);

        view.clearSets();
        view.setCurrentExerciseNode(exNode);

        totalSetsForExercise = 3;
        targetRepsForExercise = 10;
        if (exNode.getModifiers() != null) {
            for (ExerciseModifierBean mod : exNode.getModifiers()) {
                if ("Sets".equalsIgnoreCase(mod.getName())) {
                    try { totalSetsForExercise = Integer.parseInt(mod.getValue()); } catch (Exception ignored) {}
                }
                if ("Reps".equalsIgnoreCase(mod.getName())) {
                    try { targetRepsForExercise = Integer.parseInt(mod.getValue()); } catch (Exception ignored) {}
                }
            }
        }

        view.setCurrentSetNumber(currentSetNum, "0.0", String.valueOf(targetRepsForExercise));

        // Start by showing placeholders while fetching
        view.setExerciseDetails("Loading...");
        view.setInstructions("Loading...", "Fetching instructions...");

        // Fetch exercise details from the library using its resourceId
        if (exNode.getResourceId() != null) {
            exerciseLibraryManager.getExercisesAsync(List.of(exNode.getResourceId()))
                    .thenAccept(exercises -> {
                        if (!exercises.isEmpty()) {
                            Platform.runLater(() -> {
                                var ex = exercises.get(0);
                                exNode.setName(ex.getName()); // update the node so PlanNodeComponent can pick it up
                                // Refresh the node component in the view to show the new name
                                view.setCurrentExerciseNode(exNode);

                                String focus = ex.getMuscleGroups() != null ? String.join(", ", ex.getMuscleGroups()) : "N/A";
                                view.setExerciseDetails(focus);
                                view.setInstructions(ex.getName(), ex.getExecution() != null ? ex.getExecution() : "Nessuna istruzione fornita.");
                            });
                        }
                    });
        }
    }

    private void skipPrevious() {
        saveCurrentExerciseLogs();
        manager.skipPrevious();
        if (currentExerciseIndex > 0) {
            loadExercise(currentExerciseIndex - 1);
        }
    }

    private void skipNext() {
        saveCurrentExerciseLogs();
        manager.skipNext();
        if (currentExerciseIndex < exerciseNodes.size() - 1) {
            loadExercise(currentExerciseIndex + 1);
        } else {
            finishWorkoutSession();
        }
    }

    private void togglePlayPause() {
        if (isPlaying) {
            manager.pause();
            view.getBtnPlayPause().setGraphic(new Icon("play-icon", 40, List.of("button-header-icon")));
        } else {
            manager.play();
            view.getBtnPlayPause().setGraphic(new Icon("pause-icon", 40, List.of("button-header-icon")));
        }
        isPlaying = !isPlaying;
    }

    private void handleLogSet() {
        String weightStr = view.getCurrentWeight();
        String repsStr = view.getCurrentReps();
        String rpeStr = view.getCurrentRpe();
        
        double weight = 0.0;
        int reps = 0;
        try { weight = Double.parseDouble(weightStr); } catch (Exception ignored) {}
        try { reps = Integer.parseInt(repsStr); } catch (Exception ignored) {}
        
        // Save logic
        currentExerciseSets.add(new ExerciseSetDTO(reps, weight));
        
        // Add row to view
        view.addLoggedSetRow(currentSetNum, weightStr, repsStr, rpeStr);
        
        // Prepare next set
        currentSetNum++;
        if (currentSetNum <= totalSetsForExercise) {
            view.setCurrentSetNumber(currentSetNum, weightStr, String.valueOf(targetRepsForExercise));
        } else {
            // Automatically advance or show it's done? Let's just keep the form ready for "extra sets"
            // or just leave it at Set n+1
            view.setCurrentSetNumber(currentSetNum, weightStr, String.valueOf(targetRepsForExercise));
        }
    }

    private void saveCurrentExerciseLogs() {
        if (currentExerciseSets.isEmpty()) return;

        PlanNodeBean exNode = exerciseNodes.get(currentExerciseIndex);
        ExerciseLogDTO exLog = new ExerciseLogDTO(exNode.getName(), exNode.getId(), new ArrayList<>(currentExerciseSets), 8, "Log");
        exerciseLogs.add(exLog);
        currentExerciseSets.clear();
    }

    private void finishWorkoutSession() {
        saveCurrentExerciseLogs();
        
        manager.finishAndSaveSession()
                .thenRun(() -> Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Allenamento Salvato");
                    alert.setHeaderText(null);
                    alert.setContentText("Complimenti! Il tuo allenamento è stato registrato con successo sul server.");
                    alert.showAndWait();

                    Navigator.getInstance().goHome();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Navigator.getInstance().getGuiManager().showExceptionError(
                                "Errore nel salvataggio del log:", ex);
                    });
                    return null;
                });
    }
}
