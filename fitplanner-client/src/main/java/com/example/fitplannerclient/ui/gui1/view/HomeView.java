package com.example.fitplannerclient.ui.gui1.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

public class HomeView extends BaseView {
    private Button primaryButton;

    public HomeView(Node header) {
        if (header != null) this.setHeader(header);

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        VBox welcomeSection = createWelcomeSection();
        VBox workoutCard = createWorkoutCard();

        content.getChildren().addAll(welcomeSection, workoutCard);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        this.setContent(scrollPane);

    }

    private Text welcomeTitle;
    private Text welcomeSubtitle;
    private VBox createWelcomeSection() {
        VBox box = new VBox(5);

        welcomeTitle = new Text("Benvenuto in FitPlanner!");
        welcomeTitle.getStyleClass().add("h1");

        welcomeSubtitle = new Text("Oggi è il momento perfetto per superare i tuoi limiti");
        welcomeSubtitle.getStyleClass().add("subtitle");

        box.getChildren().addAll(welcomeTitle, welcomeSubtitle);
        return box;
    }

    public void setWelcomeTitle(String title) { welcomeTitle.setText(title); }

    private VBox createWorkoutCard() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(25));

        BorderPane header = new BorderPane();

        // Tag "Day 2: Pull"
        Label tag = new Label("Day 2: Pull");
        tag.getStyleClass().addAll("tag", "tag-blue");

        // Titolo Centrale
        VBox centerTitle = new VBox(5);
        centerTitle.setAlignment(Pos.CENTER);
        Label mainTitle = new Label("Today's Workout");
        mainTitle.getStyleClass().add("card-title");
        Label subTitle = new Label("Back and Biceps Focus");
        subTitle.getStyleClass().add("card-subtitle");
        centerTitle.getChildren().addAll(mainTitle, subTitle);

        // Durata
        Label duration = new Label("~60 min");
        duration.getStyleClass().add("duration-label");
        // Opzionale: Aggiungi icona orologio qui

        header.setLeft(tag);
        header.setCenter(centerTitle);
        header.setRight(duration);

        // -- Lista Esercizi --
        VBox exercisesList = new VBox(10);
        exercisesList.getChildren().addAll(
                createExerciseRow(1, "Pull-ups", "4 sets x 8-12 reps", "Bar"),
                createExerciseRow(2, "Barbell Rows", "4 sets x 8-10 reps", "Barbell"),
                createExerciseRow(3, "Lat Pulldowns", "3 sets x 10-12 reps", "Cable"),
                createExerciseRow(4, "Dumbbell Curls", "3 sets x 12-15 reps", "Dumbbells"),
                createExerciseRow(5, "Face Pulls", "3 sets x 15-20 reps", "Cable")
        );

        // -- Footer (Bottoni) --
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnReschedule = new Button("Reschedule");
        btnReschedule.getStyleClass().add("button-secondary");

        Button btnStart = new Button("Start Session");
        btnStart.getStyleClass().add("button-primary");
        // Aggiungi icona play al bottone se vuoi

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        footer.getChildren().addAll(btnReschedule, spacer, btnStart);
        HBox.setHgrow(btnStart, Priority.ALWAYS); // Il bottone start si allarga come nell'img
        btnStart.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(header, exercisesList, footer);
        return card;
    }

    private HBox createExerciseRow(int index, String name, String details, String equipment) {
        HBox row = new HBox(15);
        row.getStyleClass().add("exercise-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 15, 10, 15));

        // Indice (Cerchio o numero)
        Label idxLabel = new Label(String.valueOf(index));
        idxLabel.getStyleClass().add("index-label");

        // Nome
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("exercise-name");
        nameLabel.setMinWidth(150);

        // Dettagli (Sets/Reps) - Spazio flessibile
        Label detailsLabel = new Label(details);
        detailsLabel.getStyleClass().add("exercise-details");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Equipment Tag
        Label equipLabel = new Label(equipment);
        equipLabel.getStyleClass().add("equipment-tag");

        row.getChildren().addAll(idxLabel, nameLabel, spacer, detailsLabel, equipLabel);
        return row;
    }

    private VBox createScheduleCard() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        // -- Header --
        BorderPane header = new BorderPane();
        Label title = new Label("This Week");
        title.getStyleClass().add("card-title-small");
        Label dateRange = new Label("Nov 13 - Nov 19");
        dateRange.getStyleClass().add("card-subtitle");

        VBox titleBox = new VBox(2, title, dateRange);

        // Frecce fittizie per navigazione
        Label arrows = new Label("<   >");
        arrows.setStyle("-fx-font-weight: bold; -fx-cursor: hand;");

        header.setLeft(titleBox);
        header.setRight(arrows);

        // -- Giorni --
        VBox daysList = new VBox(10);
        daysList.getChildren().addAll(
                createDayRow("Mon", "13", "Push", "Completed", "status-success"),
                createDayRow("Tue", "14", "Pull", "Today", "status-active"),
                createDayRow("Wed", "15", "Legs", "", "status-future"),
                createDayRow("Thu", "16", "Rest", "", "status-rest"),
                createDayRow("Fri", "17", "Push", "", "status-future"),
                createDayRow("Sat", "18", "Pull", "", "status-future"),
                createDayRow("Sun", "19", "Rest", "", "status-rest")
        );

        card.getChildren().addAll(header, daysList);
        return card;
    }

    private HBox createDayRow(String dayName, String dayNum, String workoutType, String statusText, String statusClass) {
        HBox row = new HBox(10);
        row.getStyleClass().addAll("day-row", statusClass);
        row.setPadding(new Insets(10));
        row.setAlignment(Pos.CENTER_LEFT);

        VBox dateBox = new VBox(0);
        Label dayLbl = new Label(dayName);
        dayLbl.getStyleClass().add("day-name");
        Label numLbl = new Label(dayNum);
        numLbl.getStyleClass().add("day-number");
        dateBox.getChildren().addAll(dayLbl, numLbl);
        dateBox.setMinWidth(30);

        VBox infoBox = new VBox(2);
        Label typeLbl = new Label(workoutType);
        typeLbl.getStyleClass().add("workout-type");

        infoBox.getChildren().add(typeLbl);
        if (!statusText.isEmpty()) {
            Label statusLbl = new Label(statusText);
            statusLbl.getStyleClass().add("workout-status");
            infoBox.getChildren().add(statusLbl);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(dateBox, infoBox, spacer);

        // Aggiungi spunta verde se completato
        if (statusText.equals("Completed")) {
            Label check = new Label("✔");
            check.setStyle("-fx-text-fill: white; -fx-background-color: #22c55e; -fx-background-radius: 10; -fx-padding: 2 5;");
            row.getChildren().add(check);
        }

        // Aggiungi pallino blu se oggi
        if (statusText.equals("Today")) {
            Region dot = new Region();
            dot.setPrefSize(6,6);
            dot.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 50%;");
            row.getChildren().add(dot);
        }

        return row;
    }


}
