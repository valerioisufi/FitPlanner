package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.exercise.CurrentExerciseBean;
import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import com.example.fitplannerclient.bean.log.ExerciseLogBean;
import com.example.fitplannerclient.bean.log.ExerciseSetBean;
import com.example.fitplannerclient.bean.plan.ExerciseModifierBean;
import com.example.fitplannerclient.controller.plan.execution.WorkoutExecutionManager;
import com.example.fitplannerclient.controller.plan.execution.observer.WorkoutExecutionObserver;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.plan.execution.WorkoutExecutionView;
import com.example.fitplannerclient.util.ValidationUtils;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.util.List;

public class WorkoutExecutionViewController implements GuiController, WorkoutExecutionObserver {

    private static final String DEFAULT_RPE = "8";

    private final WorkoutExecutionView view;

    private final String planId;
    private final int sessionDay;

    private String currentExerciseId;
    private int currentSetNum = 1;
    private boolean isPlaying = true;

    private final Navigator navigator;
    private final GuiManager guiManager;
    private final WorkoutExecutionManager executionManager;

    private Timeline restTimeline;
    private int remainingRestSeconds;

    public WorkoutExecutionViewController(Navigator navigator, GuiManager guiManager, String planId, int sessionDay, WorkoutExecutionManager executionManager) {
        this.navigator = navigator;
        this.guiManager = guiManager;
        this.planId = planId;
        this.sessionDay = sessionDay;
        this.executionManager = executionManager;

        this.view = new WorkoutExecutionView(null);

        bindValidators();
        setupActions();
    }

    private void bindValidators() {
        view.getWeightField().setValidator(weight -> ValidationUtils.validateDecimalInRange(weight, "Peso", 0, 1000));
        view.getRepsField().setValidator(reps -> ValidationUtils.validateIntegerInRange(reps, "Reps", 1, 999));
        view.getRpeField().setValidator(rpe -> ValidationUtils.validateIntegerInRange(rpe, "RPE", 1, 10));
        view.getNotesField().setValidator(notes -> ValidationUtils.validateOptionalMaxLength(notes, "Note", 500));
        view.getSessionNotesField().setValidator(notes -> ValidationUtils.validateOptionalMaxLength(notes, "Note", 500));
    }

    private void setupActions() {
        view.setOnSkipPreviousAction(executionManager::skipPrevious);
        view.setOnPlayPauseAction(this::togglePlayPause);
        view.setOnSkipNextAction(executionManager::skipNext);
        view.setOnEndWorkoutAction(this::finishWorkoutSession);

        view.setOnDoneAction(executionManager::done);
        view.setOnSkipRestAction(executionManager::skipNext);
        view.setOnSaveSessionAction(this::finishWorkoutSession);
        view.setOnLogSetAction(this::handleLogSet);
        view.setOnNotesChanged(this::handleExerciseNotesChanged);
    }

    @Override
    public Pane getView() {
        return this.view;
    }

    @Override
    public void start() {
        executionManager.attachObserver(this);
        executionManager.startSessionAsync(planId, sessionDay)
            .thenRun(() ->
                    // Il motore provvederà a fare tick e aggiornare la UI via observer
                    Platform.runLater(executionManager::play)
            )
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

    private void togglePlayPause() {
        if (isPlaying) {
            executionManager.pause();
        } else {
            executionManager.play();
        }
    }

    private void handleLogSet() {
        if (currentExerciseId == null) return;

        // Le validazioni vengono valutate separatamente per evitare lo short-circuit,
        // così TUTTI i campi mostrano il proprio errore contemporaneamente
        boolean isWeightValid = view.getWeightField().validate();
        boolean isRepsValid = view.getRepsField().validate();
        boolean isRpeValid = view.getRpeField().validate();
        boolean isValid = isWeightValid && isRepsValid && isRpeValid;

        if (!isValid) return;

        double weight = Double.parseDouble(view.getWeight());
        int reps = Integer.parseInt(view.getReps());
        int rpe = Integer.parseInt(view.getRpe());

        executionManager.logExerciseSet(currentExerciseId, new ExerciseSetBean(reps, weight, rpe));

        view.addLoggedSetRow(currentSetNum, view.getWeight(), view.getReps(), view.getRpe());
        currentSetNum++;
        view.setCurrentSetNumber(currentSetNum);
    }

    private void handleExerciseNotesChanged(String notes) {
        if (currentExerciseId == null) return;
        if (view.getNotesField().validate()) {
            executionManager.updateExerciseNotes(currentExerciseId, notes);
        }
    }

    private void finishWorkoutSession() {
        if (!view.getSessionNotesField().validate()) return;

        executionManager.finishAndSaveSession(view.getSessionNotes())
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
    public void updateExecutionPhase(WorkoutExecutionPhase phase) {
        Platform.runLater(() -> {
            if (restTimeline != null) restTimeline.stop();

            switch (phase) {
                case EXERCISE -> view.showExerciseDetails();
                case REST -> view.showRestTimer();
                case COMPLETED -> {
                    view.setPlayerControlsDisable(true);
                    view.showSessionCompleted();
                }
            }
        });
    }

    @Override
    public void updateCurrentExercise(CurrentExerciseBean currentExercise) {
        Platform.runLater(() -> {
            ExerciseDescriptionBean description = currentExercise.getExerciseDescription();
            currentExerciseId = description.getExerciseId();

            String focus = description.getMuscleGroups() != null ? String.join(", ", description.getMuscleGroups()) : "N/A";
            String instructions = description.getExecution() != null ? description.getExecution() : "Non ci sono informazioni sull'esercizio.";
            view.setCurrentExercise(description.getName(), focus, currentExercise.getModifiers(), instructions);

            loadExerciseLog(currentExercise.getModifiers());
        });
    }

    private void loadExerciseLog(List<ExerciseModifierBean> modifiers) {
        ExerciseLogBean exerciseLog = executionManager.getSessionExerciseLog(currentExerciseId);

        view.clearSets();
        int setNum = 1;
        for (ExerciseSetBean set : exerciseLog.getSets()) {
            view.addLoggedSetRow(setNum++, String.valueOf(set.getLoad()), String.valueOf(set.getReps()), String.valueOf(set.getRpe()));
        }
        currentSetNum = setNum;
        view.setCurrentSetNumber(currentSetNum);

        prefillSetForm(exerciseLog, modifiers);
        view.setNotesText(exerciseLog.getNotes());
    }

    private void prefillSetForm(ExerciseLogBean exerciseLog, List<ExerciseModifierBean> modifiers) {
        String weight = numericModifierValue(modifiers, "WEIGHT");
        String reps = numericModifierValue(modifiers, "REPS");
        String rpe = numericModifierValue(modifiers, "RPE");

        if (!exerciseLog.getSets().isEmpty()) {
            ExerciseSetBean lastSet = exerciseLog.getSets().getLast();
            weight = String.valueOf(lastSet.getLoad());
            reps = String.valueOf(lastSet.getReps());
            rpe = String.valueOf(lastSet.getRpe());
        }

        view.setSetFormValues(weight, reps, rpe.isEmpty() ? DEFAULT_RPE : rpe);
    }

    private String numericModifierValue(List<ExerciseModifierBean> modifiers, String modifierName) {
        if (modifiers == null) return "";
        return modifiers.stream()
                .filter(mod -> modifierName.equalsIgnoreCase(mod.getName()))
                .map(ExerciseModifierBean::getValue)
                .filter(value -> value != null && value.matches("\\d+"))
                .findFirst()
                .orElse("");
    }

    @Override
    public void updateCurrentWorkoutEngineState(WorkoutExecutionState state) {
        Platform.runLater(() -> {
            if (state == WorkoutExecutionState.PLAYING) {
                isPlaying = true;
                view.setPlaying(true);

                if (restTimeline != null) restTimeline.play();
            } else if (state == WorkoutExecutionState.PAUSED) {
                isPlaying = false;
                view.setPlaying(false);

                if (restTimeline != null) restTimeline.pause();
            }
        });
    }

    @Override
    public void updateCurrentRestTime(int restTimeSeconds) {
        Platform.runLater(() -> {
            if (restTimeline != null) restTimeline.stop();

            remainingRestSeconds = restTimeSeconds;

            String timeStr = formatSeconds(restTimeSeconds);
            view.setTimerTarget(timeStr);
            view.setTimerText(timeStr);

            restTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                remainingRestSeconds--;
                if (remainingRestSeconds <= 0) {
                    restTimeline.stop();
                    // Il motore proseguirà da solo e invierà l'update del nuovo esercizio.
                } else {
                    view.setTimerText(formatSeconds(remainingRestSeconds));
                }
            }));
            restTimeline.setCycleCount(Animation.INDEFINITE);
            restTimeline.play();
        });
    }

    private String formatSeconds(int totalSeconds) {
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}
