package com.example.fitplannercommon;

import java.util.List;

public class WorkoutSessionBean {
    private String name;
    private List<ExerciseBean> exercises;
    private int day;
    private WorkoutState state = WorkoutState.TO_DO;

    public WorkoutSessionBean(String name, List<ExerciseBean> exercises, int day) {
        this.name = name;
        this.exercises = exercises;
        this.day = day;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public List<ExerciseBean> getExercises() {
        return exercises;
    }
    public void setExercises(List<ExerciseBean> exercises) {
        this.exercises = exercises;
    }
    public int getDay() {
        return day;
    }
    public void setDay(int day) {
        this.day = day;
    }
    public WorkoutState getState() {
        return state;
    }
    public void setState(WorkoutState state) {
        this.state = state;
    }
}

