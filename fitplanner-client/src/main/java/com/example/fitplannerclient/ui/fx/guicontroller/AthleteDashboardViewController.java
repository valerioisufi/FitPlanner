package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.log.WorkoutHistoryManager;
import com.example.fitplannerclient.controller.plan.WorkoutPlanManager;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.AthleteDashboardView;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AthleteDashboardViewController implements GuiController {

    private final AthleteDashboardView view;
    private final HeaderViewController headerViewController;
    private final ProfileBean athlete;
    private final WorkoutPlanManager planManager;
    private final WorkoutHistoryManager historyManager;
    private final GuiManager guiManager;

    public AthleteDashboardViewController(ProfileBean athlete, ProfileManager profileManager, WorkoutPlanManager planManager, WorkoutHistoryManager historyManager, GuiManager guiManager) {
        this.athlete = athlete;
        this.planManager = planManager;
        this.historyManager = historyManager;
        this.guiManager = guiManager;

        // Header view using index -1 (nessuna tab evidenziata) o index 0 (Home)
        this.headerViewController = new HeaderViewController(0, profileManager);
        this.view = new AthleteDashboardView();
        this.view.setHeaderView(this.headerViewController.getView());
    }

    @Override
    public void start() {
        view.setAthleteProfile(athlete);

        // Fetch the assigned plan
        planManager.getAssignedPlanOfAthleteAsync(athlete.getUserId())
                .thenAccept(plan -> Platform.runLater(() -> view.setWorkoutPlan(plan)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> guiManager.showNotification(
                            GuiManager.NotificationType.ERROR, "Errore nel caricamento del piano dell'atleta"
                    ));
                    return null;
                });

        // Fetch logs for the last 30 days
        long endTimestamp = Instant.now().toEpochMilli();
        long startTimestamp = Instant.now().minus(30, ChronoUnit.DAYS).toEpochMilli();

        historyManager.getFilteredSessionLogsAsync(athlete.getUserId(), startTimestamp, endTimestamp)
                .thenAccept(logs -> Platform.runLater(() -> view.setSessionLogs(logs)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> guiManager.showNotification(
                            GuiManager.NotificationType.ERROR, "Errore nel caricamento dei log"
                    ));
                    return null;
                });
    }

    @Override
    public void stop() {
    }

    @Override
    public Pane getView() {
        return this.view;
    }
}
