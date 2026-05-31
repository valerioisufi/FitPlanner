package com.example.fitplannerclient.ui.fx.view.dashboard;

import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.bean.plan.WorkoutSessionBean;
import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.bean.plan.NodeType;
import com.example.fitplannerclient.bean.plan.ExerciseModifierBean;
import com.example.fitplannerclient.bean.plan.WorkoutScheduleBean;
import com.example.fitplannercommon.WorkoutState;
import com.example.fitplannerclient.ui.fx.components.CardListView;
import com.example.fitplannerclient.ui.fx.view.plan.editor.PlanViewer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import java.util.List;

public class HomeView extends BorderPane {

    private final VBox contentBox;
    private final Label welcomeTitle;
    private final Label welcomeSubtitle;

    public HomeView(Node header) {
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

    public void setInviteCode(String code) {
        if (code != null) {
            VBox codeBox = new VBox(4);
            codeBox.setAlignment(Pos.CENTER_RIGHT);
            Label lbl = new Label("Il tuo Codice Invito:");
            lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: -fx-color-text-light;");
            Label codeLabel = new Label(code);
            codeLabel.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 16px; -fx-text-fill: -fx-radix-blue-11; -fx-background-color: -fx-radix-blue-3; -fx-padding: 4px 12px; -fx-background-radius: 6px;");
            codeBox.getChildren().addAll(lbl, codeLabel);

            BorderPane welcomeSection = (BorderPane) contentBox.getChildren().get(0);
            welcomeSection.setRight(codeBox);
        }
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
            dayRow.setStyle("-fx-background-radius: 12px;");

            VBox dateBox = new VBox(2);
            dateBox.setAlignment(Pos.CENTER);
            Label dowLbl = new Label(dayOfWeekStr);
            dowLbl.setStyle("-fx-font-family: 'Space Grotesk Medium'; -fx-font-size: 12px;");
            Label domLbl = new Label(dayOfMonthStr);
            domLbl.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 18px;");
            dateBox.getChildren().addAll(dowLbl, domLbl);

            VBox infoBox = new VBox(2);
            Label sessionNameLbl = new Label(sessionName);
            sessionNameLbl.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 15px;");
            
            Label statusLbl = new Label();
            statusLbl.setStyle("-fx-font-size: 12px;");
            infoBox.getChildren().addAll(sessionNameLbl, statusLbl);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label iconLbl = new Label();
            iconLbl.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 14px;");

            if (state == WorkoutState.DONE) {
                dayRow.setStyle(dayRow.getStyle() + "-fx-background-color: #E7F9CC; -fx-border-color: #D3F2A3; -fx-border-width: 1px; -fx-border-radius: 12px;");
                dowLbl.setStyle(dowLbl.getStyle() + "-fx-text-fill: #3F6212;");
                domLbl.setStyle(domLbl.getStyle() + "-fx-text-fill: #3F6212;");
                sessionNameLbl.setStyle(sessionNameLbl.getStyle() + "-fx-text-fill: #3F6212;");
                statusLbl.setText("Completed");
                statusLbl.setStyle(statusLbl.getStyle() + "-fx-text-fill: #4D7C0F;");
                iconLbl.setText("✔");
                iconLbl.setStyle(iconLbl.getStyle() + "-fx-text-fill: #FFFFFF; -fx-background-color: #22C55E; -fx-background-radius: 50%; -fx-padding: 2px 6px;");
                dayRow.getChildren().addAll(dateBox, infoBox, spacer, iconLbl);
            } else if (isToday) {
                dayRow.setStyle(dayRow.getStyle() + "-fx-background-color: #EFF6FF; -fx-border-color: #BFDBFE; -fx-border-width: 1px; -fx-border-radius: 12px;");
                dowLbl.setStyle(dowLbl.getStyle() + "-fx-text-fill: #1D4ED8;");
                domLbl.setStyle(domLbl.getStyle() + "-fx-text-fill: #1D4ED8;");
                sessionNameLbl.setStyle(sessionNameLbl.getStyle() + "-fx-text-fill: #1D4ED8;");
                statusLbl.setText("Today");
                statusLbl.setStyle(statusLbl.getStyle() + "-fx-text-fill: #3B82F6;");
                iconLbl.setText("•");
                iconLbl.setStyle(iconLbl.getStyle() + "-fx-text-fill: #3B82F6; -fx-font-size: 24px;");
                dayRow.getChildren().addAll(dateBox, infoBox, spacer, iconLbl);
            } else if (state == WorkoutState.REST || sessionName.equals("Rest")) {
                dayRow.setStyle(dayRow.getStyle() + "-fx-background-color: #F8FAFC;");
                dowLbl.setStyle(dowLbl.getStyle() + "-fx-text-fill: #94A3B8;");
                domLbl.setStyle(domLbl.getStyle() + "-fx-text-fill: #94A3B8;");
                sessionNameLbl.setStyle(sessionNameLbl.getStyle() + "-fx-text-fill: #64748B; -fx-font-weight: normal;");
                statusLbl.setManaged(false);
                dayRow.getChildren().addAll(dateBox, infoBox);
            } else {
                dayRow.setStyle(dayRow.getStyle() + "-fx-background-color: #F8FAFC;");
                dowLbl.setStyle(dowLbl.getStyle() + "-fx-text-fill: #64748B;");
                domLbl.setStyle(domLbl.getStyle() + "-fx-text-fill: #1E293B;");
                sessionNameLbl.setStyle(sessionNameLbl.getStyle() + "-fx-text-fill: #1E293B;");
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

    public void showTrainerInviteCard(java.util.function.Consumer<String> onSubmit) {
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

    public void showTrainerDashboard(Runnable onGoToLibrary, Runnable onGoToPlans) {
        while (contentBox.getChildren().size() > 1) {
            contentBox.getChildren().remove(1);
        }

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        // Card 1: Libreria Esercizi
        VBox card1 = new VBox(15);
        card1.getStyleClass().add("card");
        card1.setPrefWidth(300);
        Label title1 = new Label("Libreria Esercizi");
        title1.getStyleClass().add("heading-h2");
        Label desc1 = new Label("Visualizza, crea, modifica e cancella gli esercizi della libreria globale.");
        desc1.getStyleClass().add("body-base");
        desc1.setWrapText(true);
        Button btn1 = new Button("Gestisci Esercizi");
        btn1.getStyleClass().add("button-primary");
        btn1.setOnAction(e -> onGoToLibrary.run());
        card1.getChildren().addAll(title1, desc1, btn1);

        // Card 2: Gestione Piani
        VBox card2 = new VBox(15);
        card2.getStyleClass().add("card");
        card2.setPrefWidth(300);
        Label title2 = new Label("Gestione Piani");
        title2.getStyleClass().add("heading-h2");
        Label desc2 = new Label("Crea nuovi schemi di allenamento settimanali e assegnali ai tuoi atleti.");
        desc2.getStyleClass().add("body-base");
        desc2.setWrapText(true);
        Button btn2 = new Button("Pianifica Allenamenti");
        btn2.getStyleClass().add("button-primary");
        btn2.setOnAction(e -> onGoToPlans.run());
        card2.getChildren().addAll(title2, desc2, btn2);

        grid.add(card1, 0, 0);
        grid.add(card2, 1, 0);

        contentBox.getChildren().add(grid);
    }

    public void showAthleteList(List<com.example.fitplannerclient.bean.profile.ProfileBean> athletes, java.util.function.Consumer<com.example.fitplannerclient.bean.profile.ProfileBean> onAthleteSelected) {
        VBox athletesSection = new VBox(15);
        athletesSection.setPadding(new Insets(20, 0, 0, 0));

        Label sectionTitle = new Label("I Miei Atleti");
        sectionTitle.getStyleClass().add("heading-h2");
        athletesSection.getChildren().add(sectionTitle);

        Label nameHeader = new Label("Nome Atleta");
        nameHeader.setPrefWidth(250);
        Label emailHeader = new Label("Email");

        CardListView<com.example.fitplannerclient.bean.profile.ProfileBean> cardListView = new CardListView<>(List.of(nameHeader, emailHeader));
        
        cardListView.setRowRenderer((athlete, isLast) -> {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add(isLast ? "list-row-last" : "list-row");
            row.setStyle("-fx-cursor: hand;");
            row.setOnMouseClicked(e -> onAthleteSelected.accept(athlete));

            VBox nameBox = new VBox(4);
            nameBox.prefWidthProperty().bind(nameHeader.widthProperty());
            nameBox.minWidthProperty().bind(nameHeader.widthProperty());
            nameBox.maxWidthProperty().bind(nameHeader.widthProperty());

            Label nameLabel = new Label(athlete.getFirstName() + " " + athlete.getLastName());
            nameLabel.getStyleClass().add("body-base");
            nameBox.getChildren().addAll(nameLabel);

            Label emailLabel = new Label(athlete.getContactEmail());
            emailLabel.getStyleClass().addAll("body-small", "text-color-light");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label viewLabel = new Label("Vedi Dettagli ➔");
            viewLabel.setStyle("-fx-text-fill: -fx-radix-blue-11; -fx-font-family: 'Space Grotesk Medium'; -fx-font-size: 13px;");

            row.getChildren().addAll(nameBox, emailLabel, spacer, viewLabel);
            
            // Add hover effect
            row.setOnMouseEntered(e -> row.setStyle("-fx-cursor: hand; -fx-background-color: -fx-radix-slate-2; -fx-background-radius: 8px;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-background-radius: 8px;"));

            return row;
        });

        cardListView.setItems(athletes, "Non hai ancora nessun atleta collegato.");
        athletesSection.getChildren().add(cardListView);

        contentBox.getChildren().add(athletesSection);
    }

    private HBox createExerciseRow(int index, String name, String details, String equipment) {
        HBox row = new HBox(15);
        row.getStyleClass().add("exercise-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 18, 12, 18));
        row.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8px; -fx-border-color: #E2E8F0; -fx-border-radius: 8px;");

        Label idxLabel = new Label(String.valueOf(index));
        idxLabel.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-text-fill: -fx-radix-blue-9; -fx-font-size: 14px;");

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-family: 'Space Grotesk Medium'; -fx-text-fill: -fx-color-text-body; -fx-font-size: 14px;");
        nameLabel.setMinWidth(150);

        Label detailsLabel = new Label(details);
        detailsLabel.setStyle("-fx-text-fill: -fx-color-text-light; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label equipLabel = new Label(equipment);
        equipLabel.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #475569; -fx-padding: 2px 8px; -fx-background-radius: 4px; -fx-font-size: 11px;");

        row.getChildren().addAll(idxLabel, nameLabel, spacer, detailsLabel, equipLabel);
        return row;
    }
}
