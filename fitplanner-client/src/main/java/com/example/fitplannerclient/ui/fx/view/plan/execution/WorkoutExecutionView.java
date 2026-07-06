package com.example.fitplannerclient.ui.fx.view.plan.execution;

import com.example.fitplannerclient.bean.plan.ExerciseModifierBean;
import com.example.fitplannerclient.ui.fx.components.FormField;
import com.example.fitplannerclient.ui.fx.components.Icon;
import com.example.fitplannerclient.ui.fx.view.plan.editor.components.BadgeComponent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

public class WorkoutExecutionView extends BorderPane {

    private static final String BUTTON_HEADER_ICON = "button-header-icon";
    private static final String PLAYER_BUTTON_CLASS = "execution-player-button";
    private static final String CARD_CLASS = "card";
    private static final String BUTTON_PRIMARY_CLASS = "button-primary";
    private static final String BODY_BASE_CLASS = "body-base";
    private static final String HEADING_H3_CLASS = "heading-h3";

    // --- Esercizio corrente (sinistra) ---
    private final VBox exerciseBox = new VBox(15);
    private final Label lblExerciseName = new Label();
    private final Label lblMuscleGroups = new Label();
    private final FlowPane modifiersPane = new FlowPane(8, 8);
    private final Label lblInstructionSteps = new Label();
    private final Button btnDone = new Button("DONE");

    // --- Timer di recupero (sinistra) ---
    private final VBox restTimerBox = new VBox(20);
    private final Label lblTimerTime = new Label("00:00");
    private final Label lblTimerTarget = new Label("Target: 00:00");
    private final Button btnSkipRest = new Button("SKIP REST");

    // --- Sessione completata (sinistra) ---
    private final VBox sessionCompletedBox = new VBox(20);
    private final TextArea sessionNotesArea = new TextArea();
    private final FormField sessionNotesField = new FormField("NOTE SESSIONE", "Com'è andato l'allenamento?", sessionNotesArea);
    private final Button btnSaveSession = new Button("SALVA SESSIONE");

    // --- Log dei set (destra) ---
    private final VBox loggedSetsContainer = new VBox(8);
    private final Label lblCurrentSetHeader = new Label("Set 1");

    private final TextField weightInput = new TextField();
    private final TextField repsInput = new TextField();
    private final TextField rpeInput = new TextField();

    private final FormField weightField = new FormField("WEIGHT (KG)", "0.0", weightInput);
    private final FormField repsField = new FormField("REPS", "0", repsInput);
    private final FormField rpeField = new FormField("RPE", "8", rpeInput);

    private final TextArea notesArea = new TextArea();
    private final FormField notesField = new FormField("NOTE ATLETA", "Aggiungi note sull'esercizio...", notesArea);

    private final Button btnLogSet = new Button("LOG SET");

    // --- Controlli del player (destra) ---
    private final Button btnSkipPrevious = new Button();
    private final Button btnPlayPause = new Button();
    private final Button btnSkipNext = new Button();
    private final Button btnEndWorkout = new Button();

    public WorkoutExecutionView(Node header) {
        if (header != null) {
            this.setTop(header);
        }

        HBox mainLayout = new HBox(30);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        StackPane leftPane = createLeftPane();
        HBox.setHgrow(leftPane, Priority.ALWAYS);

        VBox rightPane = createRightPane();
        rightPane.setMinWidth(420);
        rightPane.setMaxWidth(480);

        mainLayout.getChildren().addAll(leftPane, rightPane);
        this.setCenter(mainLayout);

        showExerciseDetails();
    }

    private StackPane createLeftPane() {
        buildExerciseBox();
        buildRestTimerBox();
        buildSessionCompletedBox();

        StackPane leftPane = new StackPane(exerciseBox, restTimerBox, sessionCompletedBox);
        leftPane.setAlignment(Pos.TOP_LEFT);
        return leftPane;
    }

    private void buildExerciseBox() {
        exerciseBox.getStyleClass().add(CARD_CLASS);

        Label lblExerciseBoxTitle = new Label("Esercizio corrente");
        lblExerciseBoxTitle.getStyleClass().add("heading-h1");

        lblExerciseName.getStyleClass().add("heading-h2");
        lblExerciseName.setWrapText(true);

        lblMuscleGroups.getStyleClass().add("execution-focus-label");
        lblMuscleGroups.setWrapText(true);

        VBox header = new VBox(8, lblExerciseName, lblMuscleGroups, modifiersPane);
        header.getStyleClass().addAll("plan-node", "node-exercise");

        Label lblInstructionTitle = new Label("Come eseguire");
        lblInstructionTitle.getStyleClass().add(HEADING_H3_CLASS);

        lblInstructionSteps.getStyleClass().add(BODY_BASE_CLASS);
        lblInstructionSteps.setWrapText(true);

        ScrollPane instructionScroll = new ScrollPane(lblInstructionSteps);
        instructionScroll.setFitToWidth(true);
        instructionScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        instructionScroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(instructionScroll, Priority.ALWAYS);

        btnDone.getStyleClass().add(BUTTON_PRIMARY_CLASS);
        btnDone.setMaxWidth(Double.MAX_VALUE);

        exerciseBox.getChildren().addAll(lblExerciseBoxTitle, header, lblInstructionTitle, instructionScroll, btnDone);
    }

    private void buildRestTimerBox() {
        restTimerBox.getStyleClass().add(CARD_CLASS);
        restTimerBox.setAlignment(Pos.CENTER);

        BorderPane timerHeader = new BorderPane();
        Label lblRestTitle = new Label("REST TIME");
        lblRestTitle.getStyleClass().addAll(HEADING_H3_CLASS, "text-color-light");
        Icon clockIcon = new Icon("clock-icon", 24, List.of(BUTTON_HEADER_ICON));
        timerHeader.setLeft(lblRestTitle);
        timerHeader.setRight(clockIcon);

        lblTimerTime.getStyleClass().add("execution-timer-time");
        lblTimerTarget.getStyleClass().add(BODY_BASE_CLASS);

        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.getChildren().addAll(lblTimerTime, lblTimerTarget);
        VBox.setVgrow(centerBox, Priority.ALWAYS);

        btnSkipRest.getStyleClass().add("button-secondary");
        btnSkipRest.setMaxWidth(Double.MAX_VALUE);

        restTimerBox.getChildren().addAll(timerHeader, centerBox, btnSkipRest);
    }

    private void buildSessionCompletedBox() {
        sessionCompletedBox.getStyleClass().add(CARD_CLASS);

        Label lblCompletedTitle = new Label("Allenamento completato!");
        lblCompletedTitle.getStyleClass().add("heading-h1");

        Label lblCompletedMessage = new Label("Hai completato tutti gli esercizi della sessione. Aggiungi qualche nota e salva il log.");
        lblCompletedMessage.getStyleClass().add(BODY_BASE_CLASS);
        lblCompletedMessage.setWrapText(true);

        sessionNotesArea.setPrefRowCount(5);
        sessionNotesArea.setWrapText(true);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        btnSaveSession.getStyleClass().add(BUTTON_PRIMARY_CLASS);
        btnSaveSession.setMaxWidth(Double.MAX_VALUE);

        sessionCompletedBox.getChildren().addAll(lblCompletedTitle, lblCompletedMessage, sessionNotesField, spacer, btnSaveSession);
    }

    private VBox createRightPane() {
        VBox card = new VBox(20);
        card.getStyleClass().add(CARD_CLASS);

        Label lblSetLogTitle = new Label("Set Log");
        lblSetLogTitle.getStyleClass().add("heading-h2");

        ScrollPane setsScroll = new ScrollPane(loggedSetsContainer);
        setsScroll.setFitToWidth(true);
        setsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        setsScroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(setsScroll, Priority.ALWAYS);

        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);

        card.getChildren().addAll(lblSetLogTitle, setsScroll, createCurrentSetForm(), notesField, createPlayerControls());
        return card;
    }

    private VBox createCurrentSetForm() {
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(15));
        formBox.getStyleClass().add("execution-set-form");

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label lblCurrentSet = new Label("CURRENT SET");
        lblCurrentSet.getStyleClass().add("execution-current-set");

        lblCurrentSetHeader.getStyleClass().add(HEADING_H3_CLASS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(lblCurrentSet, spacer, lblCurrentSetHeader);

        weightInput.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("\\d*(\\.\\d*)?") ? change : null));
        repsInput.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));
        rpeInput.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));

        HBox fieldsBox = new HBox(15);
        HBox.setHgrow(weightField, Priority.ALWAYS);
        HBox.setHgrow(repsField, Priority.ALWAYS);
        HBox.setHgrow(rpeField, Priority.ALWAYS);
        fieldsBox.getChildren().addAll(weightField, repsField, rpeField);

        btnLogSet.getStyleClass().addAll(BUTTON_PRIMARY_CLASS);
        btnLogSet.setMaxWidth(Double.MAX_VALUE);

        formBox.getChildren().addAll(headerBox, fieldsBox, btnLogSet);
        return formBox;
    }

    private HBox createPlayerControls() {
        HBox playerControls = new HBox(15);
        playerControls.setAlignment(Pos.CENTER);
        playerControls.setPadding(new Insets(20, 0, 0, 0));

        btnSkipPrevious.setGraphic(new Icon("skip-previous-icon", 32, List.of(BUTTON_HEADER_ICON)));
        btnSkipPrevious.getStyleClass().add(PLAYER_BUTTON_CLASS);

        btnPlayPause.setGraphic(new Icon("pause-icon", 40, List.of(BUTTON_HEADER_ICON)));
        btnPlayPause.getStyleClass().add(PLAYER_BUTTON_CLASS);

        btnSkipNext.setGraphic(new Icon("skip-next-icon", 32, List.of(BUTTON_HEADER_ICON)));
        btnSkipNext.getStyleClass().add(PLAYER_BUTTON_CLASS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnEndWorkout.setGraphic(new Icon("stop-icon", 24, List.of(BUTTON_HEADER_ICON)));
        btnEndWorkout.getStyleClass().add("execution-btn-end");

        playerControls.getChildren().addAll(btnSkipPrevious, btnPlayPause, btnSkipNext, spacer, btnEndWorkout);
        return playerControls;
    }

    // --- Esercizio corrente ---

    public void setCurrentExercise(String name, String musclesFocus, List<ExerciseModifierBean> modifiers, String instructions) {
        lblExerciseName.setText(name);
        lblMuscleGroups.setText("FOCUS: " + musclesFocus);
        lblInstructionSteps.setText(instructions);

        modifiersPane.getChildren().clear();
        if (modifiers != null) {
            for (ExerciseModifierBean modifier : modifiers) {
                BadgeComponent.BadgeColor color = BadgeComponent.resolveColorFromName(modifier.getName(), BadgeComponent.BadgeType.MODIFIER);
                modifiersPane.getChildren().add(new BadgeComponent(modifier.getId(), BadgeComponent.BadgeType.MODIFIER, modifier.getName(), modifier.getValue(), color));
            }
        }
        boolean hasModifiers = !modifiersPane.getChildren().isEmpty();
        modifiersPane.setVisible(hasModifiers);
        modifiersPane.setManaged(hasModifiers);
    }

    public void showRestTimer() {
        exerciseBox.setVisible(false);
        restTimerBox.setVisible(true);
        sessionCompletedBox.setVisible(false);
    }

    public void showExerciseDetails() {
        exerciseBox.setVisible(true);
        restTimerBox.setVisible(false);
        sessionCompletedBox.setVisible(false);
    }

    public void showSessionCompleted() {
        exerciseBox.setVisible(false);
        restTimerBox.setVisible(false);
        sessionCompletedBox.setVisible(true);
    }

    public void setPlayerControlsDisable(boolean disable) {
        btnSkipPrevious.setDisable(disable);
        btnPlayPause.setDisable(disable);
        btnSkipNext.setDisable(disable);
    }

    public void setTimerText(String time) {
        lblTimerTime.setText(time);
    }

    public void setTimerTarget(String target) {
        lblTimerTarget.setText("Target: " + target);
    }

    // --- Log dei set ---

    public void clearSets() {
        loggedSetsContainer.getChildren().clear();
    }

    public void addLoggedSetRow(int setNum, String weight, String reps, String rpe) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));
        row.getStyleClass().add("execution-set-row");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblRpe = new Label("RPE " + rpe);
        lblRpe.getStyleClass().add("execution-rpe-chip");

        row.getChildren().addAll(
                createSetInfoBox("SET", String.valueOf(setNum)),
                createSetInfoBox("WEIGHT", weight + " kg"),
                createSetInfoBox("REPS", reps),
                spacer, lblRpe
        );
        loggedSetsContainer.getChildren().add(row);
    }

    private VBox createSetInfoBox(String title, String value) {
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("execution-set-row-title");

        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("execution-set-row-value");

        VBox box = new VBox(2);
        box.getChildren().addAll(lblTitle, lblValue);
        return box;
    }

    public void setCurrentSetNumber(int setNum) {
        lblCurrentSetHeader.setText("Set " + setNum);
    }

    public void setSetFormValues(String weight, String reps, String rpe) {
        weightField.setText(weight != null ? weight : "");
        repsField.setText(reps != null ? reps : "");
        rpeField.setText(rpe != null ? rpe : "");
    }

    public void setNotesText(String notes) {
        notesField.setText(notes != null ? notes : "");
    }

    public void setPlaying(boolean playing) {
        btnPlayPause.setGraphic(new Icon(playing ? "pause-icon" : "play-icon", 40, List.of(BUTTON_HEADER_ICON)));
    }

    // --- Actions ---

    public void setOnDoneAction(Runnable action) { btnDone.setOnAction(e -> action.run()); }
    public void setOnSkipRestAction(Runnable action) { btnSkipRest.setOnAction(e -> action.run()); }
    public void setOnLogSetAction(Runnable action) { btnLogSet.setOnAction(e -> action.run()); }
    public void setOnSkipPreviousAction(Runnable action) { btnSkipPrevious.setOnAction(e -> action.run()); }
    public void setOnPlayPauseAction(Runnable action) { btnPlayPause.setOnAction(e -> action.run()); }
    public void setOnSkipNextAction(Runnable action) { btnSkipNext.setOnAction(e -> action.run()); }
    public void setOnEndWorkoutAction(Runnable action) { btnEndWorkout.setOnAction(e -> action.run()); }
    public void setOnSaveSessionAction(Runnable action) { btnSaveSession.setOnAction(e -> action.run()); }

    public void setOnNotesChanged(Consumer<String> onNotesChanged) {
        notesArea.textProperty().addListener((obs, oldVal, newVal) -> onNotesChanged.accept(newVal));
    }

    // --- Getters ---

    public FormField getWeightField() { return weightField; }
    public FormField getRepsField() { return repsField; }
    public FormField getRpeField() { return rpeField; }
    public FormField getNotesField() { return notesField; }
    public FormField getSessionNotesField() { return sessionNotesField; }

    public String getWeight() { return weightField.getText(); }
    public String getReps() { return repsField.getText(); }
    public String getRpe() { return rpeField.getText(); }
    public String getSessionNotes() { return sessionNotesField.getText(); }
}
