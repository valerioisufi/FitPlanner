package com.example.fitplannercommon;

import java.util.List;

public class WorkoutSessionBean {
    private String name;
    private List<ExerciseBean> exercises;

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
}
