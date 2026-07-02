package com.example.fitplannerclient.entity.plan;

import java.util.List;

/**
 * Calendario del ciclo corrente del piano assegnato, con lo stato di ogni giorno
 * e la prossima sessione suggerita
 */
public record WorkoutSchedule(String planId, String planTitle, long cycleStartDate, long cycleEndDate,
                              int currentCycleDay, List<WorkoutState> workoutStates,
                              WorkoutSession nextSuggestedSession) {

    public enum WorkoutState {
        TO_DO, IN_PROGRESS, DONE, SKIPPED, REST
    }

}
