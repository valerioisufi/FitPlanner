package com.example.fitplannercommon;

import java.util.List;

public class WorkoutScheduleDTO {
    private String planId;
    private String planTitle;

    private long cycleStartDate;
    private long cycleEndDate;

    /** Giorno assoluto (dall'inizio del piano) corrispondente a oggi */
    private int todayAbsoluteDay;

    /** Un elemento per ogni giorno del ciclo corrente, in ordine */
    private List<ScheduleDayDTO> days;

    /** Giorno assoluto della prossima sessione da svolgere, -1 se nessuna */
    private int suggestedAbsoluteDay = -1;

    public WorkoutScheduleDTO() {}

    public WorkoutScheduleDTO(String planId, String planTitle, long cycleStartDate, long cycleEndDate, int todayAbsoluteDay) {
        this.planId = planId;
        this.planTitle = planTitle;

        this.cycleStartDate = cycleStartDate;
        this.cycleEndDate = cycleEndDate;
        this.todayAbsoluteDay = todayAbsoluteDay;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPlanTitle() {
        return planTitle;
    }

    public void setPlanTitle(String planTitle) {
        this.planTitle = planTitle;
    }

    public long getCycleStartDate() {
        return cycleStartDate;
    }

    public void setCycleStartDate(long cycleStartDate) {
        this.cycleStartDate = cycleStartDate;
    }

    public long getCycleEndDate() {
        return cycleEndDate;
    }

    public void setCycleEndDate(long cycleEndDate) {
        this.cycleEndDate = cycleEndDate;
    }

    public int getTodayAbsoluteDay() {
        return todayAbsoluteDay;
    }

    public void setTodayAbsoluteDay(int todayAbsoluteDay) {
        this.todayAbsoluteDay = todayAbsoluteDay;
    }

    public List<ScheduleDayDTO> getDays() {
        return days;
    }

    public void setDays(List<ScheduleDayDTO> days) {
        this.days = days;
    }

    public int getSuggestedAbsoluteDay() {
        return suggestedAbsoluteDay;
    }

    public void setSuggestedAbsoluteDay(int suggestedAbsoluteDay) {
        this.suggestedAbsoluteDay = suggestedAbsoluteDay;
    }
}
