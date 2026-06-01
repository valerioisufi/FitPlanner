package com.example.fitplannerclient.ui.fx.view.dashboard;

import com.example.fitplannerclient.bean.log.SessionLogBean;
import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.bean.profile.ProfileBean;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;

public class AthleteDashboardView extends BorderPane {

    private final VBox contentBox;
    private final Label athleteNameLabel;
    private final Label athleteEmailLabel;
    private final VBox planSection;
    private final VBox logsSection;

    public AthleteDashboardView() {
        contentBox = new VBox(24);
        contentBox.setPadding(new Insets(32));
        contentBox.setAlignment(Pos.TOP_LEFT);

        // Header Section
        VBox headerBox = new VBox(4);
        athleteNameLabel = new Label();
        athleteNameLabel.getStyleClass().add("heading-h1");
        athleteEmailLabel = new Label();
        athleteEmailLabel.setStyle("-fx-text-fill: -fx-color-text-light; -fx-font-size: 14px;");
        headerBox.getChildren().addAll(athleteNameLabel, athleteEmailLabel);
        contentBox.getChildren().add(headerBox);

        // Plan Section
        planSection = new VBox(15);
        Label planTitle = new Label("Piano Assegnato");
        planTitle.getStyleClass().add("heading-h2");
        planSection.getChildren().add(planTitle);
        contentBox.getChildren().add(planSection);

        // Logs Section
        logsSection = new VBox(15);
        Label logsTitle = new Label("Ultimi Allenamenti");
        logsTitle.getStyleClass().add("heading-h2");
        logsSection.getChildren().add(logsTitle);
        contentBox.getChildren().add(logsSection);

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        this.setCenter(scrollPane);
    }

    public void setHeaderView(Node header) {
        this.setTop(header);
    }

    public void setAthleteProfile(ProfileBean athlete) {
        athleteNameLabel.setText(athlete.getFirstName() + " " + athlete.getLastName());
        athleteEmailLabel.setText(athlete.getContactEmail());
    }

    public void setWorkoutPlan(WorkoutPlanBean plan) {
        // Clear previous plan cards
        while (planSection.getChildren().size() > 1) {
            planSection.getChildren().remove(1);
        }

        if (plan == null) {
            Label noPlan = new Label("Nessun piano assegnato a questo atleta.");
            noPlan.setStyle("-fx-text-fill: -fx-color-text-light; -fx-font-style: italic;");
            planSection.getChildren().add(noPlan);
            return;
        }

        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(20));

        Label title = new Label(plan.getName());
        title.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 16px; -fx-text-fill: -fx-color-text-body;");
        Label sessionsLabel = new Label("Numero di sessioni: " + (plan.getSessions() != null ? plan.getSessions().size() : 0));
        sessionsLabel.setStyle("-fx-text-fill: -fx-color-text-light; -fx-font-size: 14px;");

        card.getChildren().addAll(title, sessionsLabel);
        planSection.getChildren().add(card);
    }

    public void setSessionLogs(List<SessionLogBean> logs) {
        // Clear previous logs
        while (logsSection.getChildren().size() > 1) {
            logsSection.getChildren().remove(1);
        }

        if (logs == null || logs.isEmpty()) {
            Label noLogs = new Label("Nessun allenamento registrato di recente.");
            noLogs.setStyle("-fx-text-fill: -fx-color-text-light; -fx-font-style: italic;");
            logsSection.getChildren().add(noLogs);
            return;
        }

        VBox listContainer = new VBox(10);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (SessionLogBean log : logs) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("card");
            row.setStyle("-fx-padding: 15px;");

            VBox infoBox = new VBox(4);
            Label planName = new Label("Giorno Sessione: " + log.getWorkoutSessionDay());
            planName.setStyle("-fx-font-family: 'Space Grotesk Bold'; -fx-font-size: 14px; -fx-text-fill: -fx-color-text-body;");
            Label sessionName = new Label("Data: " + sdf.format(new java.util.Date(log.getDate())));
            sessionName.setStyle("-fx-text-fill: -fx-color-text-light; -fx-font-size: 13px;");
            infoBox.getChildren().addAll(planName, sessionName);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label durationLabel = new Label(log.getStatus() != null ? log.getStatus() : "Sconosciuto");
            durationLabel.setStyle("-fx-font-family: 'Space Grotesk Medium'; -fx-font-size: 12px; -fx-text-fill: -fx-radix-blue-11; -fx-background-color: -fx-radix-blue-3; -fx-padding: 4px 12px; -fx-background-radius: 6px;");

            row.getChildren().addAll(infoBox, spacer, durationLabel);
            listContainer.getChildren().add(row);
        }
        logsSection.getChildren().add(listContainer);
    }
}
