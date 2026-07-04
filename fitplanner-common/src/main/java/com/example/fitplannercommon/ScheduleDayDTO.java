package com.example.fitplannercommon;

/**
 * Un giorno del ciclo corrente
 */
public class ScheduleDayDTO {
    // absoluteDay è il giorno contato dall'inizio del piano (0-based), lo stesso identificatore usato per avviare una sessione e nei log
    private int absoluteDay;
    private WorkoutState state;

    public ScheduleDayDTO() {}

    public ScheduleDayDTO(int absoluteDay, WorkoutState state) {
        this.absoluteDay = absoluteDay;
        this.state = state;
    }

    public int getAbsoluteDay() {
        return absoluteDay;
    }

    public void setAbsoluteDay(int absoluteDay) {
        this.absoluteDay = absoluteDay;
    }

    public WorkoutState getState() {
        return state;
    }

    public void setState(WorkoutState state) {
        this.state = state;
    }
}
