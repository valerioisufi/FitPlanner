package com.example.fitplannerclient.ui.fx.view.statistics;

import com.example.fitplannerclient.bean.log.StatisticsBean;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ProgressView extends BorderPane {

    private static final String BODY_BASE_CLASS = "body-base";

    private final VBox exerciseListBox = new VBox(4);
    private LineChart<Number, Number> chart;
    private ComboBox<Period> periodCombo;

    private Consumer<Set<String>> onExerciseSelectionChange;
    private BiConsumer<Long, Long> onPeriodSelectionChange;

    public ProgressView() {
        this(false);
    }

    public ProgressView(boolean embedded) {
        VBox contentBox = new VBox(24);
        contentBox.setPadding(embedded ? Insets.EMPTY : new Insets(32));

        HBox mainArea = new HBox(24);
        mainArea.getChildren().addAll(buildExercisePanel(), buildChartPanel());

        contentBox.getChildren().addAll(buildHeader(embedded), mainArea);

        // se embedded == true, non viene aggiunto lo scroll pane
        if (embedded) {
            this.setCenter(contentBox);
        } else {
            ScrollPane mainScroll = new ScrollPane(contentBox);
            mainScroll.setFitToWidth(true);
            this.setCenter(mainScroll);
        }
    }

    public void setHeaderView(Node headerView) {
        this.setTop(headerView);
    }

    private Node buildExercisePanel() {
        VBox panel = new VBox(10);
        panel.setPrefWidth(220);
        panel.setMinWidth(Region.USE_PREF_SIZE);
        panel.getStyleClass().add("card");
        panel.setPadding(new Insets(20));

        Label title = new Label("Esercizi");
        title.getStyleClass().add(BODY_BASE_CLASS);

        ScrollPane listScroll = new ScrollPane(exerciseListBox);
        listScroll.setFitToWidth(true);
        listScroll.setPrefHeight(320);

        panel.getChildren().addAll(title, listScroll);
        return panel;
    }

    private enum Period {
        FOUR_WEEKS("Ultime 4 settimane", 28),
        THREE_MONTHS("Ultimi 3 mesi", 90),
        SIX_MONTHS("Ultimi 6 mesi", 180),
        ONE_YEAR("Ultimo anno", 365);

        private final String label;
        private final int days;

        Period(String label, int days) {
            this.label = label;
            this.days = days;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private Node buildHeader(boolean embedded) {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        periodCombo = new ComboBox<>();
        periodCombo.getItems().setAll(Period.values());
        periodCombo.setMinWidth(Region.USE_PREF_SIZE);
        periodCombo.valueProperty().addListener((obs, old, period) -> notifyPeriodChange(period));

        if (!embedded) {
            Label subtitle = new Label("Visualizza l'andamento dei tuoi allenamenti");
            subtitle.getStyleClass().addAll(BODY_BASE_CLASS, "text-color-light");
            header.getChildren().add(subtitle);
        }

        header.getChildren().addAll(spacer, periodCombo);
        return header;
    }

    private Node buildChartPanel() {
        NumberAxis axisX = new NumberAxis();
        axisX.setForceZeroInRange(false);
        axisX.setLabel("Data");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        axisX.setTickLabelFormatter(new StringConverter<>() {
            @Override
            public String toString(Number object) {
                return Instant.ofEpochMilli(object.longValue())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(formatter);
            }

            @Override
            public Number fromString(String string) {
                LocalDate data = LocalDate.parse(string, formatter);
                return data.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }
        });

        NumberAxis axisY = new NumberAxis();
        axisY.setLabel("Volume (kg)");

        chart = new LineChart<>(axisX, axisY);
        chart.setAnimated(false);
        chart.setMinHeight(350);
        VBox.setVgrow(chart, Priority.ALWAYS);

        VBox chartCard = new VBox();
        chartCard.getStyleClass().add("card");
        chartCard.setPadding(new Insets(20));
        HBox.setHgrow(chartCard, Priority.ALWAYS);
        chartCard.getChildren().add(chart);

        return chartCard;
    }

    public void setAvailableExercises(Map<String, String> exercises, Set<String> selectedIds) {
        exerciseListBox.getChildren().clear();

        exercises.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())   // ordine alfabetico per nome
                .forEach(entry -> {
                    CheckBox cb = new CheckBox(entry.getValue());
                    cb.setUserData(entry.getKey());
                    cb.getStyleClass().add(BODY_BASE_CLASS);
                    cb.setSelected(selectedIds != null && selectedIds.contains(entry.getKey()));
                    cb.selectedProperty().addListener((obs, was, is) -> notifySelectionChange());
                    exerciseListBox.getChildren().add(cb);
                });
    }

    private Set<String> getSelectedExerciseIds() {
        return exerciseListBox.getChildren().stream()
                .map(CheckBox.class::cast)
                .filter(CheckBox::isSelected)
                .map(cb -> (String) cb.getUserData())
                .collect(Collectors.toSet());
    }

    private void notifyPeriodChange(Period period) {
        if (period == null || onPeriodSelectionChange == null) return;

        ZoneId zone = ZoneId.systemDefault();
        long start = LocalDate.now(zone).minusDays(period.days)
                .atStartOfDay(zone).toInstant().toEpochMilli();

        onPeriodSelectionChange.accept(start, System.currentTimeMillis());
    }

    public void selectDefaultPeriod() {
        periodCombo.getSelectionModel().select(Period.THREE_MONTHS);
    }

    private void notifySelectionChange() {
        if (onExerciseSelectionChange != null) {
            onExerciseSelectionChange.accept(getSelectedExerciseIds());
        }
    }

    public void setOnExerciseSelectionChange(Consumer<Set<String>> callback) {
        this.onExerciseSelectionChange = callback;
    }

    public void setStatistics(List<StatisticsBean> statistics) {
        chart.getData().clear();

        for (StatisticsBean statisticsBean : statistics) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(statisticsBean.exerciseName());

            statisticsBean.sessionStatisticsList().forEach(session ->
                series.getData().add(new XYChart.Data<>(session.date(), session.totalVolume()))
            );
            chart.getData().add(series);
        }
    }

    public void setOnPeriodChange(BiConsumer<Long, Long> onPeriodChange) {
        this.onPeriodSelectionChange = onPeriodChange;
    }
}
