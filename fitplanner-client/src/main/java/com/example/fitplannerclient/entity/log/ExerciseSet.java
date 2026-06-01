package com.example.fitplannerclient.entity.log;

public record ExerciseSet(int reps, double load, int rpe) {

    public double calculateVolume() {
        return reps * load;
    }
}