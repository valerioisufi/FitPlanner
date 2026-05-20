package com.example.fitplannercommon;

import java.util.List;

public class ExerciseDescriptionDTO {
    private String exerciseId;

    private String name;
    private String execution;
    private List<String> muscleGroups;

    public ExerciseDescriptionDTO(){}

    public ExerciseDescriptionDTO(String exerciseId, String name, String execution, List<String> muscleGroups) {
        this.exerciseId = exerciseId;

        this.name = name;
        this.execution = execution;
        this.muscleGroups = muscleGroups;
    }

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