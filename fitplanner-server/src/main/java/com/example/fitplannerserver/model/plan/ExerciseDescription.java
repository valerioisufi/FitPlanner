package com.example.fitplannerserver.model.plan;

import java.util.List;

public class ExerciseDescription {
    private final String trainerId;
    private final String exerciseId;

    private String name;
    private String execution;
    private List<String> muscleGroups;

    public ExerciseDescription(String trainerId, String exerciseId) {
        this.trainerId = trainerId;
        this.exerciseId = exerciseId;
    }

    public ExerciseDescription(ExerciseDescription other){
        this.trainerId = other.trainerId;
        this.exerciseId = other.exerciseId;
        this.name = other.name;
        this.execution = other.execution;
        this.muscleGroups = other.muscleGroups;
    }

    public void setDescription(String name, String execution, List<String> muscleGroups){
        this.name = name;
        this.execution = execution;
        this.muscleGroups = muscleGroups;
    }

    public String getTrainerId() {
        return trainerId;
    }

    public String getExerciseId() { return exerciseId; }

    public String getName() { return name; }

    public String getExecution() { return execution; }

    public List<String> getMuscleGroups() { return muscleGroups; }

}
