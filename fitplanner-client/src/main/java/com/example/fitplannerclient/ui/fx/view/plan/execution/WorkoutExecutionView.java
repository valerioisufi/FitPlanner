package com.example.fitplannerclient.ui.fx.view.plan.execution;

import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.ui.fx.components.FormField;
import com.example.fitplannerclient.ui.fx.components.Icon;
import com.example.fitplannerclient.ui.fx.view.plan.editor.components.PlanNodeComponent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class WorkoutExecutionView extends BorderPane {

    private final VBox planNodeContainer = new VBox();
    private final Label lblMuscleGroups = new Label();
    private final Label lblInstructionTitle = new Label();
    private final Label lblInstructionSteps = new Label();

    private final VBox instructionBox = new VBox(15);
    private final VBox restTimerBox = new VBox(20);
    private final Label lblTimerTime = new Label("00:00");
    private final Label lblTimerTarget = new Label("Target: 00:00");
    private final Button btnSkipRest = new Button("SKIP REST");
    private final StackPane leftContentArea = new StackPane();

    private final VBox loggedSetsContainer = new VBox(8);
    private final VBox currentSetFormBox = new VBox(15);
    
    private Label lblCurrentSetHeader;
    private TextField txtWeight;
    private TextField txtReps;
    private TextField txtRpe;
    private Button btnLogSet;

    private final Button btnSkipPrevious = new Button();
    private final Button btnPlayPause = new Button();
    private final Button btnSkipNext = new Button();
    private final Button btnEndWorkout = new Button();

    public static record SetData(int setNum, double weight, int reps, boolean done) {}

    public WorkoutExecutionView(Node header) {
        if (header != null) {
            this.setTop(header);
        }

        HBox mainLayout = new HBox(30);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        VBox leftPane = createLeftPane();
        HBox.setHgrow(leftPane, Priority.ALWAYS);

        VBox rightPane = createRightPane();
        rightPane.setMinWidth(420);
        rightPane.setMaxWidth(480);

        mainLayout.getChildren().addAll(leftPane, rightPane);
        this.setCenter(mainLayout);
    }

    private VBox createLeftPane() {
        VBox pane = new VBox(20);
        pane.setAlignment(Pos.TOP_LEFT);

        planNodeContainer.getStyleClass().add("execution-plan-node-container");

        lblMuscleGroups.getStyleClass().add("execution-focus-label");
        lblMuscleGroups.setWrapText(true);

        VBox focusBox = new VBox(4);
        focusBox.setPadding(new Insets(10, 0, 0, 0));
        focusBox.getChildren().add(lblMuscleGroups);
        planNodeContainer.getChildren().add(focusBox);

        lblInstructionTitle.getStyleClass().add("heading-h2");
        lblInstructionTitle.setWrapText(true);

        lblInstructionSteps.getStyleClass().add("body-base");
        lblInstructionSteps.setWrapText(true);

        ScrollPane textScroll = new ScrollPane(lblInstructionSteps);
        textScroll.setFitToWidth(true);
        textScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        textScroll.getStyleClass().add("scroll-pane");
        VBox.setVgrow(textScroll, Priority.ALWAYS);

        instructionBox.getChildren().addAll(planNodeContainer, lblInstructionTitle, textScroll);
        
        setupRestTimerBox();

        leftContentArea.getChildren().addAll(instructionBox, restTimerBox);
        leftContentArea.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(leftContentArea, Priority.ALWAYS);

        pane.getChildren().add(leftContentArea);
        
        // Default visualizziamo le istruzioni
        showExerciseDetails();
        return pane;
    }

    private void setupRestTimerBox() {
        restTimerBox.getStyleClass().add("card");
        restTimerBox.setPadding(new Insets(30));
        restTimerBox.setAlignment(Pos.CENTER);

        BorderPane header = new BorderPane();
        Label lblRestTitle = new Label("REST TIME");
        lblRestTitle.getStyleClass().addAll("heading-h3", "text-color-light");
        Icon clockIcon = new Icon("clock-icon", 24, List.of("text-color-light")); // you might need a clock icon
        header.setLeft(lblRestTitle);
        header.setRight(clockIcon);

        lblTimerTime.getStyleClass().add("execution-timer-time");
        lblTimerTarget.getStyleClass().add("body-base");

        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.getChildren().addAll(lblTimerTime, lblTimerTarget);
        VBox.setVgrow(centerBox, Priority.ALWAYS);

        btnSkipRest.getStyleClass().addAll("button-secondary");
        btnSkipRest.setMaxWidth(Double.MAX_VALUE);

        restTimerBox.getChildren().addAll(header, centerBox, btnSkipRest);
        restTimerBox.setVisible(false);
    }

    public void showRestTimer() {
        instructionBox.setVisible(false);
        restTimerBox.setVisible(true);
    }

    public void showExerciseDetails() {
        instructionBox.setVisible(true);
        restTimerBox.setVisible(false);
    }

    public void setTimerText(String time) {
        lblTimerTime.setText(time);
    }

    public void setTimerTarget(String target) {
        lblTimerTarget.setText("Target: " + target);
    }

    public void setOnSkipRestAction(Runnable action) {
        btnSkipRest.setOnAction(e -> action.run());
    }

    private VBox createRightPane() {
        VBox card = new VBox(20);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(30));

        Label lblSetLogTitle = new Label("Set Log");
        lblSetLogTitle.getStyleClass().add("heading-h2");
        card.getChildren().add(lblSetLogTitle);

        ScrollPane setsScroll = new ScrollPane(loggedSetsContainer);
        setsScroll.setFitToWidth(true);
        setsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        setsScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        setsScroll.setBorder(null);
        // Remove padding from scrollpane inner content to avoid weird offsets
        setsScroll.setPadding(Insets.EMPTY);
        
        // We set Vgrow to always so the scroll pane takes all available height,
        // pushing the player controls to the bottom if the card expands,
        // or just filling the available space.
        VBox.setVgrow(setsScroll, Priority.ALWAYS);

        card.getChildren().add(setsScroll);

        setupCurrentSetForm();
        card.getChildren().add(currentSetFormBox);

        // Media Player Controls
        HBox playerControls = new HBox(15);
        playerControls.setAlignment(Pos.CENTER);
        playerControls.setPadding(new Insets(20, 0, 0, 0));

        btnSkipPrevious.setGraphic(new Icon("skip-previous-icon", 32, List.of("button-header-icon")));
        btnSkipPrevious.getStyleClass().add("button-transparent");
        btnSkipPrevious.setStyle("-fx-cursor: hand;");

        btnPlayPause.setGraphic(new Icon("pause-icon", 40, List.of("button-header-icon")));
        btnPlayPause.getStyleClass().add("button-transparent");
        btnPlayPause.setStyle("-fx-cursor: hand;");

        btnSkipNext.setGraphic(new Icon("skip-next-icon", 32, List.of("button-header-icon")));
        btnSkipNext.getStyleClass().add("button-transparent");
        btnSkipNext.setStyle("-fx-cursor: hand;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnEndWorkout.setGraphic(new Icon("stop-icon", 24, List.of("button-header-icon")));
        btnEndWorkout.getStyleClass().add("button-transparent");
        btnEndWorkout.setStyle("-fx-cursor: hand; -fx-background-color: -fx-radix-red-3; -fx-padding: 8px; -fx-background-radius: 50%;");
        
        playerControls.getChildren().addAll(btnSkipPrevious, btnPlayPause, btnSkipNext, spacer, btnEndWorkout);

        card.getChildren().add(playerControls);
        return card;
    }

    private void setupCurrentSetForm() {
        currentSetFormBox.setPadding(new Insets(15));
        currentSetFormBox.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-background-color: #F8FAFC; -fx-background-radius: 8px;");

        // Header
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label lblCurrentSet = new Label("CURRENT SET");
        lblCurrentSet.getStyleClass().add("execution-current-set");
        
        lblCurrentSetHeader = new Label("Set 1");
        lblCurrentSetHeader.getStyleClass().add("heading-h3");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(lblCurrentSet, spacer, lblCurrentSetHeader);

        // Fields
        HBox fieldsBox = new HBox(15);
        
        txtWeight = new TextField();
        txtWeight.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("\\d*(\\.\\d*)?") ? change : null));
        FormField weightField = new FormField("WEIGHT (KG)", "0.0", txtWeight);
        HBox.setHgrow(weightField, Priority.ALWAYS);

        txtReps = new TextField();
        txtReps.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));
        FormField repsField = new FormField("REPS", "0", txtReps);
        HBox.setHgrow(repsField, Priority.ALWAYS);

        txtRpe = new TextField();
        txtRpe.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("\\d*") ? change : null));
        txtRpe.setPromptText("8");
        FormField rpeField = new FormField("RPE", "8", txtRpe);
        HBox.setHgrow(rpeField, Priority.ALWAYS);

        fieldsBox.getChildren().addAll(weightField, repsField, rpeField);

        btnLogSet = new Button("LOG SET");
        btnLogSet.getStyleClass().addAll("button-primary", "execution-btn-log");
        btnLogSet.setMaxWidth(Double.MAX_VALUE);

        currentSetFormBox.getChildren().addAll(headerBox, fieldsBox, btnLogSet);
    }

    public void clearSets() {
        loggedSetsContainer.getChildren().clear();
    }

    public void addLoggedSetRow(int setNum, String weight, String reps, String rpe) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));
        row.setStyle("-fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");

        VBox setBox = new VBox(2);
        Label lblSetTitle = new Label("SET");
        lblSetTitle.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 10px; -fx-text-fill: -fx-color-text-light;");
        Label lblSet = new Label(String.valueOf(setNum));
        lblSet.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 14px;");
        setBox.getChildren().addAll(lblSetTitle, lblSet);

        VBox weightBox = new VBox(2);
        Label lblWeightTitle = new Label("WEIGHT");
        lblWeightTitle.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 10px; -fx-text-fill: -fx-color-text-light;");
        Label lblWeight = new Label(weight + " kg");
        lblWeight.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 14px;");
        weightBox.getChildren().addAll(lblWeightTitle, lblWeight);

        VBox repsBox = new VBox(2);
        Label lblRepsTitle = new Label("REPS");
        lblRepsTitle.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 10px; -fx-text-fill: -fx-color-text-light;");
        Label lblReps = new Label(reps);
        lblReps.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 14px;");
        repsBox.getChildren().addAll(lblRepsTitle, lblReps);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblRpe = new Label("RPE " + rpe);
        lblRpe.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 12px; -fx-background-color: -fx-radix-red-9; -fx-text-fill: white; -fx-padding: 4px 8px; -fx-background-radius: 4px;");

        Icon checkIcon = new Icon("check-icon", 16);
        checkIcon.setStyle("-fx-background-color: white;");
        Label checkLabel = new Label();
        checkLabel.setGraphic(checkIcon);
        checkLabel.setStyle("-fx-background-color: -fx-radix-green-9; -fx-background-radius: 50%; -fx-padding: 4px;");

        row.getChildren().addAll(setBox, weightBox, repsBox, spacer, lblRpe, checkLabel);
        loggedSetsContainer.getChildren().add(row);
    }

    public void setCurrentSetNumber(int setNum, String promptWeight, String promptReps) {
        lblCurrentSetHeader.setText("Set " + setNum);
        txtWeight.clear();
        txtWeight.setPromptText(promptWeight != null && !promptWeight.isEmpty() ? promptWeight : "0.0");
        txtReps.clear();
        txtReps.setPromptText(promptReps != null && !promptReps.isEmpty() ? promptReps : "0");
        txtRpe.clear();
    }

    public void setOnLogSetAction(Runnable action) {
        btnLogSet.setOnAction(e -> action.run());
    }

    public String getCurrentWeight() {
        return txtWeight.getText().isEmpty() ? txtWeight.getPromptText() : txtWeight.getText();
    }

    public String getCurrentReps() {
        return txtReps.getText().isEmpty() ? txtReps.getPromptText() : txtReps.getText();
    }

    public String getCurrentRpe() {
        return txtRpe.getText().isEmpty() ? txtRpe.getPromptText() : txtRpe.getText();
    }

    public void setCurrentExerciseNode(PlanNodeBean exerciseNode) {
        if (planNodeContainer.getChildren().size() > 1) {
            planNodeContainer.getChildren().remove(0); // Remove old node
        }
        if (exerciseNode != null) {
            PlanNodeComponent nodeComponent = new PlanNodeComponent(exerciseNode, false, null, false);
            planNodeContainer.getChildren().add(0, nodeComponent);
        }
    }

    public void setExerciseDetails(String musclesFocus) {
        lblMuscleGroups.setText("FOCUS: " + musclesFocus);
    }

    public void setInstructions(String title, String steps) {
        lblInstructionTitle.setText("Come eseguire: " + title);
        lblInstructionSteps.setText(steps);
    }

    public Button getBtnSkipPrevious() { return btnSkipPrevious; }
    public Button getBtnPlayPause() { return btnPlayPause; }
    public Button getBtnSkipNext() { return btnSkipNext; }
    public Button getBtnEndWorkout() { return btnEndWorkout; }
}
