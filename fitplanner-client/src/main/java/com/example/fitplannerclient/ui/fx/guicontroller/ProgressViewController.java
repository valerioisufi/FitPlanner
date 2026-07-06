package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.log.FilterBean;
import com.example.fitplannerclient.controller.log.WorkoutHistoryManager;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.statistics.ProgressView;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.example.fitplannerclient.controller.session.NotificationManager;

public class ProgressViewController implements GuiController {

    private final ProgressView view;
    private HeaderViewController headerViewController;

    private final WorkoutHistoryManager historyManager;
    private final GuiManager guiManager;

    // esercizi disponibili nel periodo corrente e selezione dell'utente
    private FilterBean availableFilters;
    private Set<String> selectedExerciseIds = Set.of();

    public ProgressViewController(
            Navigator navigator, GuiManager guiManager, WorkoutHistoryManager historyManager, NotificationManager notificationManager
    ) {
        this(historyManager, guiManager, false);
        this.headerViewController = new HeaderViewController(navigator, notificationManager, 2, HeaderViewController.Type.ATHLETE);
        this.view.setHeaderView(headerViewController.getView());
    }

    public ProgressViewController(WorkoutHistoryManager historyManager, GuiManager guiManager) {
        this(historyManager, guiManager, true);
    }

    private ProgressViewController(WorkoutHistoryManager historyManager, GuiManager guiManager, boolean embedded) {
        this.historyManager = historyManager;
        this.guiManager = guiManager;
        this.view = new ProgressView(embedded);
        bindActions();
    }

    private void bindActions() {
        view.setOnPeriodChange(this::loadData);
        view.setOnExerciseSelectionChange(this::updateChart);
    }

    @Override
    public void start() {
        if (headerViewController != null) {
            headerViewController.start();
        }
        view.selectDefaultPeriod();
    }

    private void loadData(Long startDate, Long endDate) {
        historyManager.getFiltersAsync(startDate, endDate)
                .thenAccept(filters ->
                    Platform.runLater(() -> {
                        this.availableFilters = filters;
                        view.setAvailableExercises(filters.exercises());

                        refreshChart();
                    })
                )
                .exceptionally(ex -> {
                    guiManager.showExceptionError("Errore nel caricamento dei dati:", ex);
                    return null;
                });
    }

    private void updateChart(Set<String> selectedExerciseIds) {
        this.selectedExerciseIds = selectedExerciseIds;
        refreshChart();
    }

    private void refreshChart() {
        if (availableFilters == null) return;

        Map<String, String> selectedExercises = new HashMap<>();
        availableFilters.exercises().forEach((id, name) -> {
            if (selectedExerciseIds.contains(id)) {
                selectedExercises.put(id, name);
            }
        });

        FilterBean statisticsFilter = new FilterBean(availableFilters.startDate(), availableFilters.endDate(), selectedExercises);
        historyManager.getStatisticsAsync(statisticsFilter)
                .thenAccept(statistics ->
                        Platform.runLater(() -> view.setStatistics(statistics))
                ).exceptionally(ex -> {
                    guiManager.showExceptionError("Errore nel caricamento delle statistiche:", ex);
                    return null;
                });

    }

    @Override
    public void stop() {
        if (headerViewController != null) {
            headerViewController.stop();
        }
    }

    @Override
    public Pane getView() {
        return this.view;
    }
}
