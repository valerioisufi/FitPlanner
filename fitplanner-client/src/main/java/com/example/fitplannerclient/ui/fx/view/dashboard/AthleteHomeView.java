package com.example.fitplannerclient.ui.fx.view.dashboard;

import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.bean.plan.WorkoutSessionBean;
import com.example.fitplannerclient.bean.plan.WorkoutScheduleBean;
import com.example.fitplannercommon.WorkoutState;
import com.example.fitplannerclient.ui.fx.view.plan.editor.PlanViewer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import java.util.List;
import java.util.function.Consumer;

public class AthleteHomeView extends BorderPane {

    private final VBox contentBox;
    private final Label welcomeTitle;
    private final Label welcomeSubtitle;

    public AthleteHomeView(Node header) {
        if (header != null) {
            this.setTop(header);
        }

        contentBox = new VBox(24);
        contentBox.setPadding(new Insets(32));
        contentBox.setAlignment(Pos.TOP_LEFT);

        // --- Welcome Section ---
        BorderPane welcomeSection = new BorderPane();
        
        VBox titles = new VBox(8);
        welcomeTitle = new Label("Benvenuto in FitPlanner!");
        welcomeTitle.getStyleClass().add("heading-h1");
        welcomeSubtitle = new Label("Oggi è il momento perfetto per superare i tuoi limiti.");
        welcomeSubtitle.getStyleClass().add("body-base");
        titles.getChildren().addAll(welcomeTitle, welcomeSubtitle);

        welcomeSection.setLeft(titles);
        contentBox.getChildren().add(welcomeSection);

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        this.setCenter(scrollPane);
    }

    public void setWelcomeMessage(String title, String subtitle) {
        welcomeTitle.setText(title);
        welcomeSubtitle.setText(subtitle);
    }

    public void showAthleteDashboard(WorkoutPlanBean plan, WorkoutScheduleBean schedule, Runnable onStartSession) {
        // Clear old dashboard cards (keep welcome section)
        while (contentBox.getChildren().size() > 1) {
            contentBox.getChildren().remove(1);
        }

        if (plan == null || schedule == null || schedule.getNextSuggestedSession() == null) {
            showNoPlanAssigned();
            return;
        }

        WorkoutSessionBean session = schedule.getNextSuggestedSession();

        HBox splitLayout = new HBox(30);
        splitLayout.setAlignment(Pos.TOP_LEFT);

        // --- Left Panel: TODAY'S WORKOUT ---
        VBox leftPanel = new VBox(20);
        leftPanel.setPrefWidth(600);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        leftPanel.getStyleClass().add("card");
        leftPanel.setPadding(new Insets(25));

        BorderPane cardHeader = new BorderPane();

        Label tag = new Label(session.getName());
        tag.getStyleClass().addAll("badge");
        tag.setStyle("-fx-background-color: -fx-radix-blue-3; -fx-text-fill: -fx-radix-blue-11; -fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 14px;");

        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER);
        Label mainTitle = new Label("TODAY'S WORKOUT");
        mainTitle.getStyleClass().add("heading-h2");
        Label subTitle = new Label(plan.getName());
        subTitle.setStyle("-fx-text-fill: -fx-color-text-light; -fx-font-size: 14px;");
        titleBox.getChildren().addAll(mainTitle, subTitle);

        Label duration = new Label("~60 min");
        duration.setStyle("-fx-font-family: 'Space Grotesk Medium'; -fx-text-fill: -fx-color-text-light; -fx-font-size: 14px;");

        cardHeader.setLeft(tag);
        cardHeader.setCenter(titleBox);
        cardHeader.setRight(duration);

        // Use PlanViewer for the session tree
        PlanViewer planViewer = new PlanViewer();
        planViewer.setEditable(false);
        planViewer.setRootNode(session.getPlanRoot());
        planViewer.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-width: 0;");
        VBox.setVgrow(planViewer, Priority.ALWAYS);
        
        // Footer (Buttons)
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnStart = new Button("Inizia Allenamento");
        btnStart.getStyleClass().add("button-primary");
        btnStart.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnStart, Priority.ALWAYS);
        btnStart.setOnAction(e -> onStartSession.run());

        footer.getChildren().add(btnStart);

        leftPanel.getChildren().addAll(cardHeader, planViewer, footer);

        // --- Right Panel: THIS WEEK ---
        VBox rightPanel = new VBox(20);
        rightPanel.setPrefWidth(350);
        rightPanel.setMinWidth(300);
        rightPanel.getStyleClass().add("card");
        rightPanel.setPadding(new Insets(25));

        Label weekTitle = new Label("THIS WEEK");
        weekTitle.getStyleClass().add("heading-h2");
        
        // Calcola l'inizio e la fine della settimana (approssimata al ciclo o ai prossimi 7 giorni)
        java.time.Instant startInst = java.time.Instant.ofEpochMilli(schedule.getCycleStartDate());
        java.time.LocalDate startDate = java.time.LocalDate.ofInstant(startInst, java.time.ZoneOffset.UTC);
        java.time.LocalDate endDate = java.time.LocalDate.ofInstant(java.time.Instant.ofEpochMilli(schedule.getCycleEndDate()), java.time.ZoneOffset.UTC);
        
        String dateRange = startDate.getMonth().name().substring(0,3) + " " + startDate.getDayOfMonth() + " - " +
                           endDate.getMonth().name().substring(0,3) + " " + endDate.getDayOfMonth();
        Label dateRangeLabel = new Label(dateRange);
        dateRangeLabel.setStyle("-fx-text-fill: -fx-color-text-light; -fx-font-size: 13px; -fx-alignment: center;");
        dateRangeLabel.setMaxWidth(Double.MAX_VALUE);

        rightPanel.getChildren().addAll(weekTitle, dateRangeLabel);

        VBox daysList = new VBox(10);
        
        List<WorkoutState> states = schedule.getWorkoutStates();
        int cycleLength = plan.getCycleLength();
        
        int absoluteStartDay = (schedule.getCurrentCycleDay() / cycleLength) * cycleLength;

        for (int i = 0; i < states.size(); i++) {
            WorkoutState state = states.get(i);
            int absoluteDay = absoluteStartDay + i;
            int relativeDay = absoluteDay % cycleLength;
            java.time.LocalDate dayDate = startDate.plusDays(absoluteDay);
            
            String dayOfWeekStr = dayDate.getDayOfWeek().name().substring(0,3);
            String dayOfMonthStr = String.valueOf(dayDate.getDayOfMonth());
            
            String sessionName = "Rest";
            if (plan.getSession(relativeDay) != null) {
                sessionName = plan.getSession(relativeDay).getName();
            }

            boolean isToday = (absoluteDay == schedule.getCurrentCycleDay());

            HBox dayRow = new HBox(15);
            dayRow.setAlignment(Pos.CENTER_LEFT);
            dayRow.setPadding(new Insets(15));
            dayRow.getStyleClass().add("workout-row");

            VBox dateBox = new VBox(2);
            dateBox.setAlignment(Pos.CENTER);
            Label dowLbl = new Label(dayOfWeekStr);
            dowLbl.setStyle("-fx-font-family: 'Space Grotesk Medium'; -fx-font-size: 12px;");
            dowLbl.getStyleClass().add("dow-label");
            Label domLbl = new Label(dayOfMonthStr);
            domLbl.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 18px;");
            domLbl.getStyleClass().add("dom-label");
            dateBox.getChildren().addAll(dowLbl, domLbl);

            VBox infoBox = new VBox(2);
            Label sessionNameLbl = new Label(sessionName);
            sessionNameLbl.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 15px;");
            sessionNameLbl.getStyleClass().add("session-label");
            
            Label statusLbl = new Label();
            statusLbl.setStyle("-fx-font-size: 12px;");
            statusLbl.getStyleClass().add("status-label");
            infoBox.getChildren().addAll(sessionNameLbl, statusLbl);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label iconLbl = new Label();
            iconLbl.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 14px;");
            iconLbl.getStyleClass().add("icon-label");

            if (state == WorkoutState.DONE) {
                dayRow.getStyleClass().add("workout-row-done");
                statusLbl.setText("Completed");
                iconLbl.setText("✔");
                dayRow.getChildren().addAll(dateBox, infoBox, spacer, iconLbl);
            } else if (isToday) {
                dayRow.getStyleClass().add("workout-row-today");
                statusLbl.setText("Today");
                iconLbl.setText("•");
                dayRow.getChildren().addAll(dateBox, infoBox, spacer, iconLbl);
            } else if (state == WorkoutState.REST || sessionName.equals("Rest")) {
                dayRow.getStyleClass().add("workout-row-rest");
                statusLbl.setManaged(false);
                dayRow.getChildren().addAll(dateBox, infoBox);
            } else {
                dayRow.getStyleClass().add("workout-row-upcoming");
                statusLbl.setManaged(false);
                dayRow.getChildren().addAll(dateBox, infoBox);
            }

            daysList.getChildren().add(dayRow);
        }

        rightPanel.getChildren().add(daysList);
        splitLayout.getChildren().addAll(leftPanel, rightPanel);

        contentBox.getChildren().add(splitLayout);
    }

    public void showNoPlanAssigned() {
        // Clear old cards
        while (contentBox.getChildren().size() > 1) {
            contentBox.getChildren().remove(1);
        }

        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));

        Label noPlanLabel = new Label("Nessun piano di allenamento attivo");
        noPlanLabel.getStyleClass().add("heading-h2");

        Label detailLabel = new Label("Richiedi un piano al tuo trainer per iniziare ad allenarti.");
        detailLabel.getStyleClass().add("body-base");

        card.getChildren().addAll(noPlanLabel, detailLabel);
        contentBox.getChildren().add(card);
    }

    public void showTrainerInviteCard(Consumer<String> onSubmit) {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(25));

        Label title = new Label("Collegati a un Trainer");
        title.getStyleClass().add("heading-h3");
        Label subtitle = new Label("Inserisci il codice invito fornito dal tuo trainer per collegarti e ricevere i suoi piani di allenamento.");
        subtitle.getStyleClass().addAll("body-base", "text-color-light");

        HBox inputBox = new HBox(10);
        javafx.scene.control.TextField codeInput = new javafx.scene.control.TextField();
        codeInput.setPromptText("Es. ABC123XYZ");
        codeInput.getStyleClass().add("text-field");
        HBox.setHgrow(codeInput, Priority.ALWAYS);

        Button submitBtn = new Button("Collegati");
        submitBtn.getStyleClass().add("button-primary");
        submitBtn.setOnAction(e -> {
            if (onSubmit != null && !codeInput.getText().isBlank()) {
                onSubmit.accept(codeInput.getText().trim());
            }
        });

        inputBox.getChildren().addAll(codeInput, submitBtn);

        card.getChildren().addAll(title, subtitle, inputBox);
        contentBox.getChildren().add(card);
    }
}
