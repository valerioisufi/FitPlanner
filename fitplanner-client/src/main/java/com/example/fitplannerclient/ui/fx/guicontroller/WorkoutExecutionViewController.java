package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import com.example.fitplannerclient.bean.plan.NodeType;
import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.controller.plan.execution.WorkoutExecutionManager;
import com.example.fitplannerclient.controller.plan.execution.observer.WorkoutExecutionObserver;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.components.Icon;
import com.example.fitplannerclient.ui.fx.view.plan.execution.WorkoutExecutionView;
import com.example.fitplannerclient.bean.log.ExerciseLogBean;
import com.example.fitplannerclient.bean.log.ExerciseSetBean;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class WorkoutExecutionViewController implements GuiController, WorkoutExecutionObserver {

    private static final String BUTTON_HEADER_ICON = "button-header-icon";

    private final WorkoutExecutionView view;

    private final String planId;
    private final int sessionDay;
    private final List<PlanNodeBean> exerciseNodes = new ArrayList<>();
    private final List<ExerciseLogBean> exerciseLogs = new ArrayList<>();
    private final List<ExerciseSetBean> currentExerciseSets = new ArrayList<>();

    private int currentExerciseIndex = 0;
    private int currentSetNum = 1;
    private int totalSetsForExercise = 3;
    private int targetRepsForExercise = 10;
    private boolean isPlaying = true;

    private final Navigator navigator;
    private final WorkoutExecutionManager executionManager;
    private final ExerciseLibraryManager exerciseLibraryManager;
    private final GuiManager guiManager;

    private Timeline restTimeline;
    private int remainingRestSeconds;

    public WorkoutExecutionViewController(Navigator navigator, String planId, int sessionDay, WorkoutExecutionManager executionManager, ExerciseLibraryManager exerciseLibraryManager, GuiManager guiManager) {
        this.navigator = navigator;
        this.planId = planId;
        this.sessionDay = sessionDay;
        this.executionManager = executionManager;
        this.exerciseLibraryManager = exerciseLibraryManager;
        this.guiManager = guiManager;

        this.view = new WorkoutExecutionView(null);

        setupButtons();
    }

    private void setupButtons() {
        this.view.getBtnSkipPrevious().setOnAction(e -> skipPrevious());
        this.view.getBtnPlayPause().setOnAction(e -> togglePlayPause());
        this.view.getBtnSkipNext().setOnAction(e -> skipNext());
        this.view.getBtnEndWorkout().setOnAction(e -> finishWorkoutSession());
        
        this.view.setOnLogSetAction(this::handleLogSet);
        this.view.setOnSkipRestAction(this::skipNext);
    }

    @Override
    public Pane getView() {
        return this.view;
    }

    @Override
    public void start() {
        executionManager.attachObserver(this);
        executionManager.startSessionAsync(planId, sessionDay)
            .thenRun(() -> {
                Platform.runLater(() -> {
                    executionManager.play();
                    PlanNodeBean rootBean = executionManager.getSessionRootBeanForUi();
                    collectExerciseNodes(rootBean);
            
                    if (exerciseNodes.isEmpty()) {
                        navigator.goHome();
                    } else {
                        // Il motore provvederà a fare tick e aggiornare la UI via observer
                    }
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> {
                    guiManager.showExceptionError("Errore caricamento sessione", ex);
                    navigator.goHome();
                });
                return null;
            });
    }

    @Override
    public void stop() {
        executionManager.detachObserver(this);
        if (restTimeline != null) restTimeline.stop();
    }

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
            view.getBtnPlayPause().setGraphic(new Icon("play-icon", 40, List.of(BUTTON_HEADER_ICON)));
        } else {
            executionManager.play();
            view.getBtnPlayPause().setGraphic(new Icon("pause-icon", 40, List.of(BUTTON_HEADER_ICON)));
        }
        isPlaying = !isPlaying;
    }

    private void handleLogSet() {
        String weightStr = view.getCurrentWeight();
        String repsStr = view.getCurrentReps();
        String rpeStr = view.getCurrentRpe();
        
        double weight = 0.0;
        int reps = 0;
        int rpe = 0;
        try { weight = Double.parseDouble(weightStr); } catch (NumberFormatException ignored) { /* ignored */ }
        try { reps = Integer.parseInt(repsStr); } catch (NumberFormatException ignored) { /* ignored */ }
        try { rpe = Integer.parseInt(rpeStr); } catch (NumberFormatException ignored) { /* ignored */ }

        // Save logic
        currentExerciseSets.add(new ExerciseSetBean(reps, weight, rpe));
        
        // Add row to view
        view.addLoggedSetRow(currentSetNum, weightStr, repsStr, rpeStr);
        
        // Prepare next set
        currentSetNum++;
        view.setCurrentSetNumber(currentSetNum, weightStr, String.valueOf(targetRepsForExercise));
    }

    private void saveCurrentExerciseLogs() {
        if (currentExerciseSets.isEmpty()) return;

        PlanNodeBean exNode = exerciseNodes.get(currentExerciseIndex);
        ExerciseLogBean exLog = new ExerciseLogBean(exNode.getName(), exNode.getId(), new ArrayList<>(currentExerciseSets), "Log");
        exerciseLogs.add(exLog);
        currentExerciseSets.clear();
    }

    private void finishWorkoutSession() {
        saveCurrentExerciseLogs();
        
        executionManager.finishAndSaveSession()
                .thenRun(() -> Platform.runLater(() -> {
                    guiManager.showNotification(GuiManager.NotificationType.SUCCESS, "Allenamento salvato con successo!");
                    navigator.goHome();
                }))
                .exceptionally(ex -> {
                    guiManager.showExceptionError("Errore nel salvataggio del log:", ex);
                    return null;
                });
    }

    @Override
    public void updateCurrentExercise(ExerciseDescriptionBean description) {
        Platform.runLater(() -> {
            if (restTimeline != null) restTimeline.stop();
            view.showExerciseDetails();
            String focus = description.getMuscleGroups() != null ? String.join(", ", description.getMuscleGroups()) : "N/A";
            view.setExerciseDetails(focus);
            view.setInstructions(description.getName(), description.getExecution() != null ? description.getExecution() : "Nessuna istruzione fornita.");
        });
    }

    @Override
    public void updateCurrentWorkoutEngineState(WorkoutExecutionState state) {
        Platform.runLater(() -> {
            if (state == WorkoutExecutionState.PLAYING) {
                isPlaying = true;
                view.getBtnPlayPause().setGraphic(new Icon("pause-icon", 40, List.of(BUTTON_HEADER_ICON)));
            } else if (state == WorkoutExecutionState.PAUSED) {
                isPlaying = false;
                view.getBtnPlayPause().setGraphic(new Icon("play-icon", 40, List.of(BUTTON_HEADER_ICON)));
            }
        });
    }

    @Override
    public void updateCurrentRestTime(int restTimeSeconds) {
        Platform.runLater(() -> {
            if (restTimeline != null) restTimeline.stop();
            
            view.showRestTimer();
            remainingRestSeconds = restTimeSeconds;
            
            int min = restTimeSeconds / 60;
            int sec = restTimeSeconds % 60;
            String timeStr = String.format("%02d:%02d", min, sec);
            view.setTimerTarget(timeStr);
            view.setTimerText(timeStr);
            
            restTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                remainingRestSeconds--;
                if (remainingRestSeconds <= 0) {
                    restTimeline.stop();
                    // Il motore proseguirà da solo e invierà l'update del nuovo esercizio.
                } else {
                    int m = remainingRestSeconds / 60;
                    int s = remainingRestSeconds % 60;
                    view.setTimerText(String.format("%02d:%02d", m, s));
                }
            }));
            restTimeline.setCycleCount(Timeline.INDEFINITE);
            restTimeline.play();
        });
    }
}
