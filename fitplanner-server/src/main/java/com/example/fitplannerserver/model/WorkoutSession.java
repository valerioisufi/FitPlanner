package com.example.fitplannerserver.model;

import com.example.fitplannercommon.WorkoutState;

import java.util.List;

public class WorkoutSession {
    private String title;
    private List<Exercise> exercises;
    private String day;
    private WorkoutState state = WorkoutState.TO_DO;

    public WorkoutSession(String title, List<Exercise> exercises, String day, WorkoutState state) {
        this.title = title;
        this.exercises = exercises;
        this.day = day;
        this.state= state;
    }

    public String getTitle() {
        return title;
    }
    public List<Exercise> getExercises() {
        return exercises;
    }
    public String getDay() {
        return day;
    }
    public WorkoutState getState() {
        return state;
    }
    public void setState(WorkoutState state) {
        this.state = state;
    }

}