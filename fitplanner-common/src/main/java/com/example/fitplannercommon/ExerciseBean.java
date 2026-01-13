package com.example.fitplannercommon;

public class ExerciseBean {
    int type;

    int rep;
    int set;
    int rest;
    String technique;
    String notes;

    public ExerciseBean(int type, int rep, int set, int rest, String technique, String notes) {
        this.type = type;
        this.rep = rep;
        this.set = set;
        this.rest = rest;
        this.technique = technique;
        this.notes = notes;
    }

    public int getType() {
        return type;
    }
    public int getRep() {
        return rep;
    }
    public int getSet() {
        return set;
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
