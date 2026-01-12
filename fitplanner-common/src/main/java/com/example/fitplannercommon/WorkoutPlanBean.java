package com.example.fitplannercommon;

import java.util.List;

public class WorkoutPlanBean {
    private String name;
    private String description;
    private List<WorkoutSessionBean> workoutSessions;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public List<WorkoutSessionBean> getWorkoutSessions() {
        return workoutSessions;
    }
    public void setWorkoutSessions(List<WorkoutSessionBean> workoutSessions) {
        this.workoutSessions = workoutSessions;
    }

}
