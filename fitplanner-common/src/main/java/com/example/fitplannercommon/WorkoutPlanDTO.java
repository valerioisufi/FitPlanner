package com.example.fitplannercommon;

import java.util.List;

public class WorkoutPlanDTO {
    private String planId;

    private String name;
    private int cycleLength;
    private List<WorkoutSessionDTO> workoutSessions;

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

    public List<WorkoutSessionDTO> getWorkoutSessions() {
        return workoutSessions;
    }
    public void setWorkoutSessions(List<WorkoutSessionDTO> workoutSessions) {
        this.workoutSessions = workoutSessions;
    }

}
