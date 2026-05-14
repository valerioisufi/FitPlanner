package com.example.fitplannerserver.model.log;

import java.util.ArrayList;
import java.util.List;

public class ExerciseLog {
    private String name;
    private String exerciseId;

    private List<ExerciseSet> sets;

    private int rpe;

    private String notes;

    public ExerciseLog(String name, String exerciseId, List<ExerciseSet> sets, int rpe, String notes) {
        this.name = name;
        this.exerciseId = exerciseId;

        this.sets = (sets != null) ? sets : new ArrayList<>();
        this.rpe = rpe;

        this.notes = notes;
    }

    public ExerciseLog(ExerciseLog other) {
        this.name = other.name;
        this.exerciseId = other.exerciseId;
        this.rpe = other.rpe;
        this.notes = other.notes;

        this.sets = new ArrayList<>();

        if (other.sets != null) {
            for (ExerciseSet set : other.sets) {
                this.sets.add(new ExerciseSet(set.reps(), set.load()));

            }
        }

    }

    public String getName() { return name; }

    public String getExerciseId() { return exerciseId; }

    public List<ExerciseSet> getSets() { return sets; }

    public int getRpe() { return rpe; }

    public String getNotes() { return notes; }

    public double exerciseVolume() {
        double volume = 0;
        for (ExerciseSet set : this.sets) {
            volume += set.reps() * set.load();
        }
        return volume;
    }

    public record ExerciseSet(int reps, double load) {}

}