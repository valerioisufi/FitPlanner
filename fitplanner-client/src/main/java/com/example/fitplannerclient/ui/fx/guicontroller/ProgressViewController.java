package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.log.FilterBean;
import com.example.fitplannerclient.controller.log.WorkoutHistoryManager;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.statistics.ProgressView;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ProgressViewController implements GuiController {

    private final ProgressView view;
    private final HeaderViewController headerViewController;

    private final WorkoutHistoryManager historyManager;
    private final GuiManager guiManager;

    // esercizi disponibili nel periodo corrente e selezione dell'utente
    private FilterBean availableFilters;
    private Set<String> selectedExerciseIds = Set.of();

    public ProgressViewController(Navigator navigator, WorkoutHistoryManager historyManager, ProfileManager profileManager, GuiManager guiManager) {
        this.historyManager = historyManager;
        this.guiManager = guiManager;

        this.headerViewController = new HeaderViewController(navigator, 2, profileManager);
        this.view = new ProgressView();
        this.view.setHeaderView(this.headerViewController.getView());

        bindActions();
    }

    private void bindActions() {
        view.setOnPeriodChange(this::loadData);
        view.setOnExerciseSelectionChange(this::updateChart);
    }

    @Override
    public void start() {
        // innesca il primo loadData attraverso il callback del periodo
        view.selectDefaultPeriod();
    }

    private void loadData(Long startDate, Long endDate) {
        historyManager.getFiltersAsync(startDate, endDate)
                .thenAccept(filters ->
                    Platform.runLater(() -> {
                        this.availableFilters = filters;
                        view.setAvailableExercises(filters.exercises());
                        // la vista preserva la selezione senza notificarla: il grafico va riallineato qui
                        refreshChart();
                    })
                )
                .exceptionally(ex -> {
                    Platform.runLater(() -> guiManager.showExceptionError("Errore nel caricamento dei dati:", ex));
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
                .thenAccept(statistics -> Platform.runLater(() -> view.setStatistics(statistics)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> guiManager.showExceptionError("Errore nel caricamento delle statistiche:", ex));
                    return null;
                });
    }

    @Override
    public void stop() {
        // nessuna risorsa da rilasciare
    }

    @Override
    public Pane getView() {
        return this.view;
    }
}
