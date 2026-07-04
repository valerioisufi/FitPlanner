package com.example.fitplannerclient.bean.plan;

/**
 * Un giorno del ciclo corrente
 */
public class ScheduleDayBean {

    /** Giorno assoluto dall'inizio del piano: identifica la sessione da avviare */
    private int absoluteDay;

    /** Data di calendario del giorno (epoch millis, UTC) */
    private long date;

    private WorkoutState state;

    private boolean today;

    /** Sessione prevista per questo giorno, null nei giorni di riposo */
    private WorkoutSessionBean session;


    public int getAbsoluteDay() {
        return absoluteDay;
    }

    public void setAbsoluteDay(int absoluteDay) {
        this.absoluteDay = absoluteDay;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public WorkoutState getState() {
        return state;
    }

    public void setState(WorkoutState state) {
        this.state = state;
    }

    public boolean isToday() {
        return today;
    }

    public void setToday(boolean today) {
        this.today = today;
    }

    public WorkoutSessionBean getSession() {
        return session;
    }

    public void setSession(WorkoutSessionBean session) {
        this.session = session;
    }
}
