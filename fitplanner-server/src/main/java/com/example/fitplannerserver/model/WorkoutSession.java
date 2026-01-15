package com.example.fitplannerserver.model;

import com.example.fitplannercommon.WorkoutState;

import java.util.List;

public class WorkoutSession {
    private int id;
    private String title;
    private List<String> label;
    private List<Exercise> exercises;
    private int day;
    private WorkoutState state = WorkoutState.TO_DO;

    public WorkoutSession(int id, String title, List<String> label ,List<Exercise> exercises, int day, WorkoutState state) {
        this.id = id;
        this.title = title;
        this.label = label;
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

    public int getDay() {
        return day;
    }

    public WorkoutState getState() {
        return state;
    }

    public void setState(WorkoutState state) {
        this.state = state;
    }

    public void addLabel(String label){
        this.label.add(label);
    }

}