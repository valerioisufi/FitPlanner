package com.example.fitplannerclient.bean.exercise;

import java.util.List;

public class ExerciseDescriptionBean {
    private String exerciseId;

    private String name;
    private String execution;
    private List<String> muscleGroups;

    public String getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getExecution() { return execution; }

    public void setExecution(String execution) { this.execution = execution; }

    public List<String> getMuscleGroups() { return muscleGroups; }

    public void setMuscleGroups(List<String> muscleGroups) { this.muscleGroups = muscleGroups; }

}
