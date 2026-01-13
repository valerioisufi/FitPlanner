package com.example.fitplannerserver.model;

public class Exercise {
    private final int reps;
    private final int sets;
    private final int rest;
    private final String technique;
    private final String notes;

    public Exercise(int reps, int sets, int rest, String technique, String notes) {
        this.reps = reps;
        this.sets = sets;
        this.rest = rest;
        this.technique = technique;
        this.notes = notes;
    }

    public int getReps() {
        return reps;
    }
    public int getSets() {
        return sets;
    }
    public int getRest() {
        return rest;
    }
    public String getTechnique() {
        return technique;
    }
    public String getNotes() {
        return notes;
    }

}
