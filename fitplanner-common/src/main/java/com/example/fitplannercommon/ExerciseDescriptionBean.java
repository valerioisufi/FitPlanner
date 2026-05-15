package com.example.fitplannercommon;

import java.util.List;

public class ExerciseDescriptionBean {
    private String name;
    private String execution;
    private List<String> muscleGroups;

    public ExerciseDescriptionBean(){}

    public ExerciseDescriptionBean(String name, String execution, List<String> muscleGroups) {
        this.name = name;
        this.execution = execution;
        this.muscleGroups = muscleGroups;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getExecution() { return execution; }

    public void setExecution(String execution) { this.execution = execution; }

    public List<String> getMuscleGroups() { return muscleGroups; }

    public void setMuscleGroups(List<String> muscleGroups) { this.muscleGroups = muscleGroups; }

}