package com.example.fitplannerclient.ui.fx.view.dashboard;

import com.example.fitplannerclient.bean.plan.ScheduleDayBean;
import com.example.fitplannerclient.bean.plan.WorkoutScheduleBean;
import com.example.fitplannerclient.bean.plan.WorkoutState;
import com.example.fitplannerclient.ui.fx.components.FormField;
import com.example.fitplannerclient.ui.fx.view.plan.editor.PlanViewer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.function.IntConsumer;

public class AthleteHomeView extends BorderPane {

    private static final String BODY_BASE_CLASS = "body-base";
    private static final String HEADING_H2_CLASS = "heading-h2";
    private static final String REST_LABEL = "Rest";

    private final VBox contentBox;
    private final Label welcomeTitle;
    private final Label welcomeSubtitle;

    private IntConsumer onStartSessionAction;
    private Runnable onTrainerInviteSubmitAction;
    private final FormField inviteCodeField;

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
        welcomeSubtitle.getStyleClass().add(BODY_BASE_CLASS);
        titles.getChildren().addAll(welcomeTitle, welcomeSubtitle);

        welcomeSection.setLeft(titles);
        contentBox.getChildren().add(welcomeSection);

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        this.setCenter(scrollPane);

        TextField codeInput = new TextField();
        codeInput.getStyleClass().add("text-field");
        inviteCodeField = new FormField(null, "Es. ABC1-XY2Z", codeInput);
        HBox.setHgrow(inviteCodeField, Priority.ALWAYS);
    }

    public void setWelcomeMessage(String title, String subtitle) {
        welcomeTitle.setText(title);
        welcomeSubtitle.setText(subtitle);
    }

    public void setOnStartSessionAction(IntConsumer action) { this.onStartSessionAction = action; }
    public void setOnTrainerInviteSubmitAction(Runnable action) { this.onTrainerInviteSubmitAction = action; }
    public FormField getInviteCodeField() { return this.inviteCodeField; }

    public void showAthleteDashboard(WorkoutScheduleBean schedule) {
        // Clear old dashboard cards (keep welcome section)
        while (contentBox.getChildren().size() > 1) {
            contentBox.getChildren().remove(1);
        }

        HBox splitLayout = new HBox(30);
        splitLayout.setAlignment(Pos.TOP_LEFT);

        // --- Left Panel: TODAY'S WORKOUT ---
        VBox leftPanel = new VBox(20);
        leftPanel.setPrefWidth(600);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        leftPanel.getStyleClass().add("card");
        leftPanel.setPadding(new Insets(25));

        if (schedule.getSuggestedDayIndex() < 0) {
            showNoSuggestedDay(leftPanel, schedule.getPlanTitle());
        } else {
            ScheduleDayBean suggestedDay = schedule.getDays().get(schedule.getSuggestedDayIndex());
            updateLeftPanel(leftPanel, schedule.getPlanTitle(), suggestedDay);
        }

        // --- Right Panel: THIS WEEK ---
        VBox rightPanel = buildRightPanel(schedule, leftPanel);

        splitLayout.getChildren().addAll(leftPanel, rightPanel);

        contentBox.getChildren().add(splitLayout);
    }

    private VBox buildRightPanel(WorkoutScheduleBean schedule, VBox leftPanel) {
        VBox rightPanel = new VBox(20);
        rightPanel.setPrefWidth(350);
        rightPanel.setMinWidth(300);
        rightPanel.getStyleClass().add("card");
        rightPanel.setPadding(new Insets(25));

        Label weekTitle = new Label("THIS WEEK");
        weekTitle.getStyleClass().add(HEADING_H2_CLASS);

        LocalDate startDate = toLocalDate(schedule.getCycleStartDate());
        LocalDate endDate = toLocalDate(schedule.getCycleEndDate());

        String dateRange = monthAbbrev(startDate) + " " + startDate.getDayOfMonth() + " - " +
                           monthAbbrev(endDate) + " " + endDate.getDayOfMonth();
        Label dateRangeLabel = new Label(dateRange);
        dateRangeLabel.getStyleClass().add("dashboard-date-range");
        dateRangeLabel.setMaxWidth(Double.MAX_VALUE);

        rightPanel.getChildren().addAll(weekTitle, dateRangeLabel);

        VBox daysList = new VBox(10);
        for (ScheduleDayBean day : schedule.getDays()) {
            daysList.getChildren().add(buildDayRow(day, schedule.getPlanTitle(), leftPanel));
        }

        rightPanel.getChildren().add(daysList);
        return rightPanel;
    }

    private HBox buildDayRow(ScheduleDayBean day, String planTitle, VBox leftPanel) {
        LocalDate dayDate = toLocalDate(day.getDate());
        boolean isRest = day.getState() == WorkoutState.REST || day.getSession() == null;
        String sessionName = isRest ? REST_LABEL : day.getSession().getName();

        HBox dayRow = new HBox(15);
        dayRow.setAlignment(Pos.CENTER_LEFT);
        dayRow.setPadding(new Insets(15));
        dayRow.getStyleClass().add("workout-row");

        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER);
        Label dowLbl = new Label(dowAbbrev(dayDate));
        dowLbl.getStyleClass().add("dow-label");
        Label domLbl = new Label(String.valueOf(dayDate.getDayOfMonth()));
        domLbl.getStyleClass().add("dom-label");
        dateBox.getChildren().addAll(dowLbl, domLbl);

        VBox infoBox = new VBox(2);
        Label sessionNameLbl = new Label(sessionName);
        sessionNameLbl.getStyleClass().add("session-label");

        Label statusLbl = new Label();
        statusLbl.getStyleClass().add("status-label");
        infoBox.getChildren().addAll(sessionNameLbl, statusLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label iconLbl = new Label();
        iconLbl.getStyleClass().add("icon-label");

        if (day.getState() == WorkoutState.DONE) {
            dayRow.getStyleClass().add("workout-row-done");
            statusLbl.setText("Completed");
            iconLbl.setText("✔");
            dayRow.getChildren().addAll(dateBox, infoBox, spacer, iconLbl);
        } else if (day.isToday()) {
            dayRow.getStyleClass().add("workout-row-today");
            statusLbl.setText("Today");
            iconLbl.setText("•");
            dayRow.getChildren().addAll(dateBox, infoBox, spacer, iconLbl);
        } else if (isRest) {
            dayRow.getStyleClass().add("workout-row-rest");
            statusLbl.setManaged(false);
            dayRow.getChildren().addAll(dateBox, infoBox);
        } else {
            dayRow.getStyleClass().add("workout-row-upcoming");
            statusLbl.setManaged(false);
            dayRow.getChildren().addAll(dateBox, infoBox);
        }

        if (!isRest) {
            dayRow.setOnMouseClicked(e -> updateLeftPanel(leftPanel, planTitle, day));
            dayRow.setStyle("-fx-cursor: hand;");
        }

        return dayRow;
    }

    private void updateLeftPanel(VBox leftPanel, String planTitle, ScheduleDayBean day) {
        leftPanel.getChildren().clear();

        BorderPane cardHeader = new BorderPane();

        Label tag = new Label(day.getSession().getName());
        tag.getStyleClass().addAll("badge", "dashboard-tag");

        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER);
        Label mainTitle = new Label(day.isToday() ? "IL PIANO DI OGGI" : titleDate(day.getDate()));
        mainTitle.getStyleClass().add(HEADING_H2_CLASS);
        Label subTitle = new Label(planTitle);
        subTitle.getStyleClass().add("body-small");
        titleBox.getChildren().addAll(mainTitle, subTitle);

        cardHeader.setLeft(tag);
        cardHeader.setCenter(titleBox);

        PlanViewer planViewer = new PlanViewer();
        planViewer.setEditable(false);
        planViewer.setRootNode(day.getSession().getPlanRoot());
        planViewer.getStyleClass().add("dashboard-plan-viewer");
        VBox.setVgrow(planViewer, Priority.ALWAYS);

        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnStart = new Button("Inizia Allenamento");
        btnStart.getStyleClass().add("button-primary");
        btnStart.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnStart, Priority.ALWAYS);
        btnStart.setOnAction(e -> {
            if (onStartSessionAction != null) onStartSessionAction.accept(day.getAbsoluteDay());
        });

        footer.getChildren().add(btnStart);

        leftPanel.getChildren().addAll(cardHeader, planViewer, footer);
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
        noPlanLabel.getStyleClass().add(HEADING_H2_CLASS);

        Label detailLabel = new Label("Richiedi un piano al tuo trainer per iniziare ad allenarti.");
        detailLabel.getStyleClass().add(BODY_BASE_CLASS);

        card.getChildren().addAll(noPlanLabel, detailLabel);
        contentBox.getChildren().add(card);
    }

    public void showNoSuggestedDay(VBox leftPanel, String planTitle) {
        leftPanel.getChildren().clear();

        BorderPane cardHeader = new BorderPane();

        VBox titleBox = new VBox(4);
        titleBox.setAlignment(Pos.CENTER);
        Label mainTitle = new Label("Nessun allenamento suggerito per oggi.");
        mainTitle.getStyleClass().add(HEADING_H2_CLASS);
        Label subTitle = new Label(planTitle);
        subTitle.getStyleClass().add("body-small");
        titleBox.getChildren().addAll(mainTitle, subTitle);

        cardHeader.setCenter(titleBox);

        leftPanel.getChildren().addAll(cardHeader);


    }

    public void showTrainerInviteCard() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(25));

        Label title = new Label("Collegati a un Trainer");
        title.getStyleClass().add("heading-h3");
        Label subtitle = new Label("Inserisci il codice invito fornito dal tuo trainer per collegarti e ricevere i suoi piani di allenamento.");
        subtitle.getStyleClass().addAll(BODY_BASE_CLASS, "text-color-light");

        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.TOP_LEFT);

        Button submitBtn = new Button("Collegati");
        submitBtn.getStyleClass().add("button-primary");
        submitBtn.setOnAction(e -> {
            if (onTrainerInviteSubmitAction != null) {
                onTrainerInviteSubmitAction.run();
            }
        });

        inputBox.getChildren().addAll(inviteCodeField, submitBtn);

        card.getChildren().addAll(title, subtitle, inputBox);
        contentBox.getChildren().add(card);
    }

    // --- formattazione date ---

    private static LocalDate toLocalDate(long epochMillis) {
        return LocalDate.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC);
    }

    private static String monthAbbrev(LocalDate date) {
        return date.getMonth().name().substring(0, 3);
    }

    private static String dowAbbrev(LocalDate date) {
        return date.getDayOfWeek().name().substring(0, 3);
    }

    private static String titleDate(long epochMillis) {
        LocalDate date = toLocalDate(epochMillis);
        return (date.getDayOfMonth() + " " + monthAbbrev(date) + " " + dowAbbrev(date)).toUpperCase();
    }
}
