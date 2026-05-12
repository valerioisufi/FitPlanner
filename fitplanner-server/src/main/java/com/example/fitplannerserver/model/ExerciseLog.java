package com.example.fitplannerserver.model;

import java.util.List;

public class ExerciseLog {
    private String name;
    private int exerciseId;

    private List<Integer> reps;
    private List<Integer> load;

    private int rpe;

    private String notes;

    ExerciseLog(String name, int exerciseId, List<Integer> reps, List<Integer> load, int rpe, String notes) {
        this.name = name;
        this.exerciseId = exerciseId;

        this.reps = reps;
        this.load = load;
        this.rpe = rpe;

        this.notes = notes;
    }

    public int exerciseVolume(){
        int volume = 0;

        if (this.reps != null && this.load != null && this.reps.size() == this.load.size()) {
            for (int i = 0; i < this.reps.size(); i++) {
                volume += this.reps.get(i) * this.load.get(i);
            }
        }
        return volume;
    }


}
