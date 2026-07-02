package com.example.fitplannerclient.bean.log;

public class ExerciseSetBean {
    private int reps;
    private double load;
    private int rpe;

    public ExerciseSetBean() {}

    public ExerciseSetBean(int reps, double load, int rpe) {
        this.reps = reps;
        this.load = load;
        this.rpe = rpe;
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

    public int getRpe() {
        return rpe;
    }

    public void setRpe(int rpe) {
        this.rpe = rpe;
    }
}
