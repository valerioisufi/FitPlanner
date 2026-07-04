package com.example.fitplannerclient.bean.plan;

import java.util.List;

public class WorkoutScheduleBean {
    private String planId;
    private String planTitle;

    private long cycleStartDate;
    private long cycleEndDate;

    /** Un elemento per ogni giorno del ciclo corrente, in ordine */
    private List<ScheduleDayBean> days;

    /** Indice in days della prossima sessione da svolgere, -1 se nessuna */
    private int suggestedDayIndex = -1;

    public WorkoutScheduleBean() {
        // Default constructor for bean
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

    public List<ScheduleDayBean> getDays() {
        return days;
    }

    public void setDays(List<ScheduleDayBean> days) {
        this.days = days;
    }

    public int getSuggestedDayIndex() {
        return suggestedDayIndex;
    }

    public void setSuggestedDayIndex(int suggestedDayIndex) {
        this.suggestedDayIndex = suggestedDayIndex;
    }
}
