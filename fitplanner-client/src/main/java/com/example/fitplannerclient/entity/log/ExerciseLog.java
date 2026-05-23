package com.example.fitplannerclient.entity.log;

import java.util.List;

public class ExerciseLog {
    private final String name;
    private final String exerciseId;

    private final List<ExerciseSet> sets;
    private final int rpe;
    private final String notes;

    public ExerciseLog(String name, String exerciseId, List<ExerciseSet> sets, int rpe, String notes) {
        this.name = name;
        this.exerciseId = exerciseId;
        this.sets = sets;
        this.rpe = rpe;
        this.notes = notes;
    }

    public double calculateTotalVolume() {
        return sets.stream().mapToDouble(ExerciseSet::calculateVolume).sum();
    }

    public String getExerciseId() { return exerciseId; }
    public List<ExerciseSet> getSets() { return sets; }
}