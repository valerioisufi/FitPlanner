package com.example.fitplannercommon;

import java.util.List;

public class WorkoutPlanBean {
    private String planId;

    private String name;
    private int cycleLength;
    private List<WorkoutSessionBean> workoutSessions;

    public String getPlanId() {
        return planId;
    }
    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getCycleLength() {
        return cycleLength;
    }
    public void setCycleLength(int cycleLength) {
        this.cycleLength = cycleLength;
    }

    public List<WorkoutSessionBean> getWorkoutSessions() {
        return workoutSessions;
    }
    public void setWorkoutSessions(List<WorkoutSessionBean> workoutSessions) {
        this.workoutSessions = workoutSessions;
    }

}
