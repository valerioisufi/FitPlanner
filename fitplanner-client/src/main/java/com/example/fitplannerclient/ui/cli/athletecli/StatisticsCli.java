package com.example.fitplannerclient.ui.cli.athletecli;

import com.example.fitplannerclient.bean.log.FilterBean;
import com.example.fitplannerclient.bean.log.StatisticsBean;
import com.example.fitplannerclient.controller.log.WorkoutHistoryManager;
import com.example.fitplannerclient.ui.cli.AbstractCliView;
import com.example.fitplannerclient.ui.cli.CliView;
import com.example.fitplannerclient.ui.cli.DashboardCli;
import com.example.fitplannerclient.ui.cli.trainercli.AthletesCli;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StatisticsCli extends AbstractCliView {

    private WorkoutHistoryManager historyManager;

    private final String athleteId;

    public StatisticsCli() {
        this.athleteId = null;
    }

    public StatisticsCli(String athleteId) {
        this.athleteId = athleteId;
    }

    @Override
    protected CliView render() {
        this.historyManager = (athleteId != null)
                ? engine.getSessionContext().createWorkoutHistoryManagerFor(athleteId)
                : engine.getSessionContext().createWorkoutHistoryManager();

        printer.printHeader(athleteId != null ? "STATISTICHE ATLETA" : "STATISTICHE");
        printer.printMenu(null, List.of("Indietro", "Visualizza statistiche ultimi 30 giorni", "Visualizza statistiche ultimi 90 giorni"));
        int scelta = reader.readInt("Scegli un'opzione: ", 1, 3); // todo accettare un numero qualsiasi di giorni come filtro
        switch (scelta) {
            case 1 -> { return getBackCli(); }
            case 2 -> viewStatistics(30);
            case 3 -> viewStatistics(90);
        }

        return this;
    }

    private CliView getBackCli() {
        return athleteId != null ? new AthletesCli() : new DashboardCli();
    }

    private void viewStatistics(int days) {
        long end = System.currentTimeMillis();
        long start = end - (days * 24L * 60L * 60L * 1000L);

        try {
            FilterBean filters = historyManager.getFiltersAsync(start, end).join();
            if (filters.exercises().isEmpty()) {
                printer.printInfo("Nessun dato disponibile in questo periodo.");
                reader.waitForEnter();
                return;
            }

            List<Map.Entry<String, String>> entries = new ArrayList<>(filters.exercises().entrySet());
            Optional<Map.Entry<String, String>> selected = reader.selectFrom(
                    "Seleziona l'esercizio per visualizzare i progressi:", entries, Map.Entry::getValue, "Indietro");
            if (selected.isEmpty()) return;

            String selectedId = selected.get().getKey();
            String selectedName = selected.get().getValue();

            FilterBean statFilter = new FilterBean(start, end, Map.of(selectedId, selectedName));
            List<StatisticsBean> statsList = historyManager.getStatisticsAsync(statFilter).join();

            if (statsList.isEmpty() || statsList.getFirst().sessionStatisticsList().isEmpty()) {
                printer.printInfo("Nessuna statistica trovata per questo esercizio.");
            } else {
                printer.printInfo("Statistiche per: " + selectedName);
                List<String> headers = Arrays.asList("Data", "Volume Totale (kg)");
                List<List<String>> data = new ArrayList<>();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        .withZone(ZoneId.systemDefault());

                for (StatisticsBean.SessionStatisticsBean sessionStat : statsList.getFirst().sessionStatisticsList()) {
                    data.add(Arrays.asList(
                            formatter.format(Instant.ofEpochMilli(sessionStat.date())),
                            String.format("%.2f", sessionStat.totalVolume())
                    ));
                }
                printer.printTable(headers, data);
            }

            reader.waitForEnter();

        } catch (Exception e) {
            printer.printException("Errore durante il recupero delle statistiche: ", e);
            reader.waitForEnter();
        }
    }
}
