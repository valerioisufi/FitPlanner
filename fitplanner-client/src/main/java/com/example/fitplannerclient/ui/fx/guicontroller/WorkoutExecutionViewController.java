package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.ui.fx.view.plan.execution.WorkoutExecutionView;
import com.example.fitplannerclient.controller.plan.execution.WorkoutExecutionManager;
import com.example.fitplannercommon.ExerciseLogDTO;
import com.example.fitplannercommon.ExerciseSetDTO;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import com.example.fitplannerclient.ui.fx.components.Icon;

import java.util.ArrayList;
import java.util.List;

public class WorkoutExecutionViewController implements GuiController {

    private final WorkoutExecutionView view;

    private final String planId;
    private final int sessionDay;
    private final List<PlanNodeBean> exerciseNodes = new ArrayList<>();
    private final List<ExerciseLogDTO> exerciseLogs = new ArrayList<>();
    private final List<ExerciseSetDTO> currentExerciseSets = new ArrayList<>();

    private int currentExerciseIndex = 0;
    private int currentSetNum = 1;
    private int totalSetsForExercise = 3;
    private int targetRepsForExercise = 10;
    private boolean isPlaying = true;

    private final WorkoutExecutionManager executionManager;
    private final ExerciseLibraryManager exerciseLibraryManager;

    public WorkoutExecutionViewController(String planId, int sessionDay, WorkoutExecutionManager executionManager, ExerciseLibraryManager exerciseLibraryManager) {
        this.planId = planId;
        this.sessionDay = sessionDay;
        this.executionManager = executionManager;
        this.exerciseLibraryManager = exerciseLibraryManager;

        this.view = new WorkoutExecutionView(null);

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
        return this.view;
    }

    @Override
    public void start() {
        executionManager.startSessionAsync(planId, sessionDay)
            .thenRun(() -> {
                Platform.runLater(() -> {
                    executionManager.play();
                    PlanNodeBean rootBean = executionManager.getSessionRootBeanForUi();
                    collectExerciseNodes(rootBean);
            
                    if (exerciseNodes.isEmpty()) {
                        Navigator.getInstance().goHome();
                    } else {
                        loadExercise(0);
                    }
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    Navigator.getInstance().getGuiManager().showExceptionError("Errore caricamento sessione", ex);
                    Navigator.getInstance().goHome();
                });
                return null;
            });
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
        executionManager.skipPrevious();
    }

    private void skipNext() {
        saveCurrentExerciseLogs();
        executionManager.skipNext();
    }

    private void togglePlayPause() {
        if (isPlaying) {
            executionManager.pause();
            view.getBtnPlayPause().setGraphic(new Icon("play-icon", 40, List.of("button-header-icon")));
        } else {
            executionManager.play();
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
        
        executionManager.finishAndSaveSession()
                .thenRun(() -> Platform.runLater(() -> {
                    Navigator.getInstance().getGuiManager().showNotification(GuiManager.NotificationType.SUCCESS, "Allenamento salvato con successo!");
                    Navigator.getInstance().goHome();
                }))
                .exceptionally(ex -> {
                    Navigator.getInstance().getGuiManager().showExceptionError("Errore nel salvataggio del log:", ex);
                    return null;
                });
    }
}
