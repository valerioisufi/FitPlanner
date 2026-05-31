package com.example.fitplannerclient.ui.fx.view.execution;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import java.util.ArrayList;
import java.util.List;

public class WorkoutExecutionView extends BorderPane {

    private final WebView youtubeWebView = new WebView();
    private final Label lblInstructionTitle = new Label();
    private final Label lblInstructionSteps = new Label();

    private final Label lblExerciseName = new Label();
    private final Label lblHistory = new Label();
    private final VBox setsContainer = new VBox(8);

    private final Button btnRestTimer = new Button("Avvia Timer Recupero");
    private final Button btnFinish = new Button("Esercizio Successivo");

    private final List<GridPane> rowList = new ArrayList<>();

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

        youtubeWebView.setPrefSize(500, 320);
        youtubeWebView.setMaxSize(Double.MAX_VALUE, 400);
        youtubeWebView.setStyle("-fx-border-color: #E2E8F0; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        lblInstructionTitle.getStyleClass().add("heading-h2");
        lblInstructionTitle.setWrapText(true);

        lblInstructionSteps.getStyleClass().add("body-base");
        lblInstructionSteps.setWrapText(true);

        ScrollPane textScroll = new ScrollPane(lblInstructionSteps);
        textScroll.setFitToWidth(true);
        textScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        textScroll.setBorder(null);

        pane.getChildren().addAll(youtubeWebView, lblInstructionTitle, textScroll);
        return pane;
    }

    private VBox createRightPane() {
        VBox card = new VBox(20);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(30));

        VBox header = new VBox(6);
        lblExerciseName.getStyleClass().add("heading-h1");
        lblHistory.getStyleClass().add("body-base");
        lblHistory.setStyle("-fx-text-fill: -fx-color-text-light;");
        header.getChildren().addAll(lblExerciseName, lblHistory);

        GridPane gridHeader = new GridPane();
        gridHeader.setHgap(10);
        gridHeader.setPadding(new Insets(0, 0, 10, 0));

        ColumnConstraints col1 = new ColumnConstraints(40);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints(); col3.setHgrow(Priority.ALWAYS);
        ColumnConstraints col4 = new ColumnConstraints(60); col4.setHalignment(javafx.geometry.HPos.CENTER);

        gridHeader.getColumnConstraints().addAll(col1, col2, col3, col4);

        gridHeader.add(createHeaderLabel("Set"), 0, 0);
        gridHeader.add(createHeaderLabel("Peso (kg)"), 1, 0);
        gridHeader.add(createHeaderLabel("Reps Target"), 2, 0);
        gridHeader.add(createHeaderLabel("Fatto"), 3, 0);

        VBox buttons = new VBox(12);
        btnRestTimer.getStyleClass().add("button-primary");
        btnRestTimer.setMaxWidth(Double.MAX_VALUE);

        btnFinish.getStyleClass().add("button-secondary");
        btnFinish.setMaxWidth(Double.MAX_VALUE);

        buttons.getChildren().addAll(btnRestTimer, btnFinish);

        card.getChildren().addAll(header, gridHeader, setsContainer, new Separator(), buttons);
        return card;
    }

    private Label createHeaderLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-text-fill: -fx-color-text-body; -fx-font-size: 12px;");
        return l;
    }

    public void clearSets() {
        setsContainer.getChildren().clear();
        rowList.clear();
    }

    public void addSetRow(int setNum, String prevWeight, String targetReps) {
        GridPane row = new GridPane();
        row.setHgap(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 0, 6, 0));
        row.setStyle("-fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");

        ColumnConstraints col1 = new ColumnConstraints(40);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints(); col3.setHgrow(Priority.ALWAYS);
        ColumnConstraints col4 = new ColumnConstraints(60); col4.setHalignment(javafx.geometry.HPos.CENTER);
        row.getColumnConstraints().addAll(col1, col2, col3, col4);

        Label lblSet = new Label(String.valueOf(setNum));
        lblSet.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 13px;");

        TextField txtWeight = new TextField();
        txtWeight.setPromptText(prevWeight);
        txtWeight.setPrefHeight(36);
        txtWeight.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6;");

        TextField txtReps = new TextField();
        txtReps.setPromptText(targetReps);
        txtReps.setPrefHeight(36);
        txtReps.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6;");

        CheckBox chkDone = new CheckBox();
        chkDone.setStyle("-fx-cursor: hand;");

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
        rowList.add(row);
    }

    public List<SetData> getLoggedSets() {
        List<SetData> data = new ArrayList<>();
        for (GridPane row : rowList) {
            try {
                Label lblSet = (Label) row.getChildren().get(0);
                TextField txtWeight = (TextField) row.getChildren().get(1);
                TextField txtReps = (TextField) row.getChildren().get(2);
                CheckBox chkDone = (CheckBox) row.getChildren().get(3);

                int setNum = Integer.parseInt(lblSet.getText());
                double weight = txtWeight.getText().isEmpty() ? Double.parseDouble(txtWeight.getPromptText()) : Double.parseDouble(txtWeight.getText());
                int reps = txtReps.getText().isEmpty() ? Integer.parseInt(txtReps.getPromptText()) : Integer.parseInt(txtReps.getText());
                boolean done = chkDone.isSelected();

                data.add(new SetData(setNum, weight, reps, done));
            } catch (Exception e) {
                // Ignore rows that don't match
            }
        }
        return data;
    }

    public void setExerciseDetails(String name, String history) {
        lblExerciseName.setText(name);
        lblHistory.setText(history);
    }

    public void setInstructions(String title, String steps) {
        lblInstructionTitle.setText("Come eseguire: " + title);
        lblInstructionSteps.setText(steps);
    }

    public void setVideoUrl(String embedUrl) {
        if (embedUrl != null && !embedUrl.isEmpty()) {
            youtubeWebView.getEngine().load(embedUrl);
        }
    }

    public void setOnFinishAction(Runnable action) {
        btnFinish.setOnAction(e -> action.run());
    }

    public void setFinishButtonText(String text) {
        btnFinish.setText(text);
    }
}
