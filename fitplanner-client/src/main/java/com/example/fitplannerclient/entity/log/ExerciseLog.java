package com.example.fitplannerclient.entity.log;

import java.util.List;

public class ExerciseLog {
    private final String name;
    private final String exerciseId;

    private final List<ExerciseSet> sets;
    private String notes;

    public ExerciseLog(String name, String exerciseId, List<ExerciseSet> sets, String notes) {
        this.name = name;
        this.exerciseId = exerciseId;
        this.sets = sets;
        this.notes = notes;
    }

    public double calculateTotalVolume() {
        return sets.stream().mapToDouble(ExerciseSet::calculateVolume).sum();
    }

    public String getExerciseId() { return exerciseId; }
    public String getName() { return name; }
    public List<ExerciseSet> getSets() { return sets; }

    public String getNotes() { return notes; }
    public void updateNotes(String notes){
        this.notes = notes;
    }

    public void addSets(List<ExerciseSet> newSets) {
        this.sets.addAll(newSets);
    }

}