package com.example.fitplannerclient.entity.log;

public class ExerciseSet {
    private final int reps;
    private final double load;

    public ExerciseSet(int reps, double load) {
        this.reps = reps;
        this.load = load;
    }

    public double calculateVolume() {
        return reps * load;
    }


    public int getReps() { return reps; }
    public double getLoad() { return load; }
}