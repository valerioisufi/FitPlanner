package com.example.fitplannercommon;

import java.util.List;

public class WorkoutScheduleBean {
    private String planId;
    private String planTitle;

    private long cycleStartDate;
    private long cycleEndDate;

    private int currentCycleDay;

    private List<WorkoutState> workoutStates;

    private WorkoutSessionBean nextSuggestedSession;

    public WorkoutScheduleBean() {}

    public WorkoutScheduleBean(String planId, String planTitle, long cycleStartDate, long cycleEndDate, int currentCycleDay) {
        this.planId = planId;
        this.planTitle = planTitle;

        this.cycleStartDate = cycleStartDate;
        this.cycleEndDate = cycleEndDate;
        this.currentCycleDay = currentCycleDay;
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

    public int getCurrentCycleDay() {
        return currentCycleDay;
    }

    public void setCurrentCycleDay(int currentCycleDay) {
        this.currentCycleDay = currentCycleDay;
    }

    public List<WorkoutState> getWorkoutStates() {
        return workoutStates;
    }

    public void setWorkoutStates(List<WorkoutState> workoutStates) {
        this.workoutStates = workoutStates;
    }

    public WorkoutSessionBean getNextSuggestedSession() {
        return nextSuggestedSession;
    }

    public void setNextSuggestedSession(WorkoutSessionBean nextSuggestedSession) {
        this.nextSuggestedSession = nextSuggestedSession;
    }

}