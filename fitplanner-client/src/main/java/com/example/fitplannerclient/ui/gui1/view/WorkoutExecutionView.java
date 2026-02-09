package com.example.fitplannerclient.ui.gui1.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebView;

public class WorkoutExecutionView extends BaseView {

    // --- Componenti Sinistra (Istruzioni) ---
    private final WebView youtubeWebView = new WebView();
    private final Label lblInstructionTitle = new Label();
    private final Label lblInstructionSteps = new Label();

    // --- Componenti Destra (Tracking) ---
    private final Label lblExerciseName = new Label();
    private final Label lblHistory = new Label();
    private final VBox setsContainer = new VBox(); // Conterrà le righe dei set

    private final Button btnRestTimer = new Button("Start Rest Timer");
    private final Button btnFinish = new Button("Finish Exercise");

    public WorkoutExecutionView() {
        // Layout principale diviso in due (Sinistra: 55%, Destra: 45%)
        HBox mainLayout = new HBox(30);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // 1. Pannello Sinistro (Video + Istruzioni)
        VBox leftPane = createLeftPane();
        HBox.setHgrow(leftPane, Priority.ALWAYS); // Prende più spazio

        // 2. Pannello Destro (Card Tracking)
        VBox rightPane = createRightPane();
        rightPane.setMinWidth(400);
        rightPane.setMaxWidth(450);

        mainLayout.getChildren().addAll(leftPane, rightPane);

        this.setContent(mainLayout);
    }

    private VBox createLeftPane() {
        VBox pane = new VBox(20);
        pane.setAlignment(Pos.TOP_LEFT);

        // --- Video Player (WebView) ---
        // Impostiamo una dimensione fissa o proporzionale
        youtubeWebView.setPrefSize(600, 640);
        youtubeWebView.setMaxSize(Double.MAX_VALUE, 640);

        // --- Testi Istruzioni ---
        lblInstructionTitle.getStyleClass().add("h2");
        lblInstructionTitle.setWrapText(true);

        lblInstructionSteps.getStyleClass().add("paragraph");
        lblInstructionSteps.setWrapText(true);
        // ScrollPane per il testo lungo
        ScrollPane textScroll = new ScrollPane(lblInstructionSteps);
        textScroll.setFitToWidth(true);
        textScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        textScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        textScroll.setBorder(null);

        pane.getChildren().addAll(youtubeWebView, lblInstructionTitle, textScroll);
        return pane;
    }

    private VBox createRightPane() {
        VBox card = new VBox();
        card.getStyleClass().add("card"); // Card bianca
        card.setPadding(new Insets(30));
        card.setSpacing(20);

        // Header
        VBox header = new VBox(5);
        lblExerciseName.getStyleClass().add("h1");

        lblHistory.getStyleClass().add("subtitle");
        header.getChildren().addAll(lblExerciseName, lblHistory);

        // Tabella dei Set (Header)
        GridPane gridHeader = new GridPane();
        gridHeader.setHgap(10);
        gridHeader.setPadding(new Insets(0, 0, 10, 0));

        // Definisci larghezze colonne: Set(10%), Weight(35%), Reps(35%), Done(20%)
        ColumnConstraints col1 = new ColumnConstraints(40);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints(); col3.setHgrow(Priority.ALWAYS);
        ColumnConstraints col4 = new ColumnConstraints(50); col4.setHalignment(javafx.geometry.HPos.CENTER);

        gridHeader.getColumnConstraints().addAll(col1, col2, col3, col4);

        gridHeader.add(createHeaderLabel("Set"), 0, 0);
        gridHeader.add(createHeaderLabel("Weight (kg)"), 1, 0);
        gridHeader.add(createHeaderLabel("Reps"), 2, 0);
        gridHeader.add(createHeaderLabel("Done"), 3, 0);

        // Contenitore righe dinamiche
        setsContainer.setSpacing(0); // Gestito dai bordi CSS

        // Footer Buttons
        VBox buttons = new VBox(10);
        btnRestTimer.getStyleClass().add("button-primary");
        btnRestTimer.setMaxWidth(Double.MAX_VALUE);
        // Icona orologio (opzionale)
        // btnRestTimer.setGraphic(new FontIcon("..."));

        btnFinish.getStyleClass().add("button-secondary");
        btnFinish.setMaxWidth(Double.MAX_VALUE);

        buttons.getChildren().addAll(btnRestTimer, btnFinish);

        card.getChildren().addAll(header, gridHeader, setsContainer, new Separator(), buttons);
        return card;
    }

    private Label createHeaderLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("sets-header-label");
        return l;
    }

    /**
     * Aggiunge una riga alla tabella dei set
     */
    public void addSetRow(int setNum, String prevWeight, String targetReps) {
        GridPane row = new GridPane();
        row.getStyleClass().add("set-row");
        row.setHgap(10);
        row.setAlignment(Pos.CENTER_LEFT);

        // Colonne allineate con l'header
        ColumnConstraints col1 = new ColumnConstraints(40);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints(); col3.setHgrow(Priority.ALWAYS);
        ColumnConstraints col4 = new ColumnConstraints(50); col4.setHalignment(javafx.geometry.HPos.CENTER);
        row.getColumnConstraints().addAll(col1, col2, col3, col4);

        // 1. Numero Set
        Label lblSet = new Label(String.valueOf(setNum));
        lblSet.getStyleClass().add("set-number-label");

        // 2. Input Peso
        TextField txtWeight = new TextField();
        txtWeight.setPromptText(prevWeight); // Es. "0" o peso precedente
        txtWeight.getStyleClass().add("execution-input");
        txtWeight.setPrefHeight(40);

        // 3. Input Reps
        TextField txtReps = new TextField();
        txtReps.setPromptText(targetReps); // Es. "0"
        txtReps.getStyleClass().add("execution-input");
        txtReps.setPrefHeight(40);

        // 4. Checkbox
        CheckBox chkDone = new CheckBox();
        chkDone.setPrefSize(24, 24);
        chkDone.setStyle("-fx-cursor: hand;");

        // Logica semplice UI: quando checkato, opacizza la riga
        chkDone.selectedProperty().addListener((obs, old, isSelected) -> {
            if (isSelected) {
                txtWeight.setDisable(true);
                txtReps.setDisable(true);
                row.setOpacity(0.5);
            } else {
                txtWeight.setDisable(false);
                txtReps.setDisable(false);
                row.setOpacity(1.0);
            }
        });

        row.add(lblSet, 0, 0);
        row.add(txtWeight, 1, 0);
        row.add(txtReps, 2, 0);
        row.add(chkDone, 3, 0);

        setsContainer.getChildren().add(row);
    }

    // --- Setters per i dati ---

    public void setExerciseDetails(String name, String history) {
        lblExerciseName.setText(name);
        lblHistory.setText(history);
    }

    public void setInstructions(String title, String steps) {
        lblInstructionTitle.setText("How to perform the " + title);
        lblInstructionSteps.setText(steps);
    }


    public void setOnFinishAction(Runnable action) {
        btnFinish.setOnAction(e -> action.run());
    }
}