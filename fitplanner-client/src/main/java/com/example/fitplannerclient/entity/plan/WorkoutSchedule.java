package com.example.fitplannerclient.entity.plan;

import java.util.List;

/**
 * Calendario del ciclo corrente del piano assegnato: un elemento per ogni
 * giorno del ciclo, il giorno assoluto di oggi e quello della prossima
 * sessione suggerita (-1 se nessuna)
 */
public record WorkoutSchedule(String planId, String planTitle, long cycleStartDate, long cycleEndDate,
                              int todayAbsoluteDay, List<ScheduleDay> days, int suggestedAbsoluteDay) {

    public enum WorkoutState {
        TO_DO, IN_PROGRESS, DONE, SKIPPED, REST
    }

    /** Un giorno del ciclo, identificato dal giorno assoluto dall'inizio del piano */
    public record ScheduleDay(int absoluteDay, WorkoutState state) {
    }

}
