package com.example.fitplannercommon;

public class ExerciseSetBean {
    private int reps;
    private double load;

    public ExerciseSetBean() {}

    public ExerciseSetBean(int reps, double load) {
        this.reps = reps;
        this.load = load;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public double getLoad() {
        return load;
    }

    public void setLoad(double load) {
        this.load = load;
    }
}