package com.example.fitplannercommon;

import java.util.List;

public class ExerciseLogBean {
    private String name;
    private String exerciseId;

    private List<ExerciseSetBean> sets;
    private int rpe;

    private String notes;

    public ExerciseLogBean() {}

    public ExerciseLogBean(String name, String exerciseId, List<ExerciseSetBean> sets, int rpe, String notes) {
        this.name = name;
        this.exerciseId = exerciseId;
        this.sets = sets;
        this.rpe = rpe;
        this.notes = notes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
    }

    public List<ExerciseSetBean> getSets() {
        return sets;
    }

    public void setSets(List<ExerciseSetBean> sets) {
        this.sets = sets;
    }

    public int getRpe() {
        return rpe;
    }

    public void setRpe(int rpe) {
        this.rpe = rpe;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

}
