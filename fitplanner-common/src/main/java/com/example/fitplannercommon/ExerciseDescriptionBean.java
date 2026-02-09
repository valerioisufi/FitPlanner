package com.example.fitplannercommon;

import java.util.List;

public class ExerciseDescriptionBean {
    private String name;
    private String description;
    private List<String> muscleGroups;

    public ExerciseDescriptionBean(String name, String description, List<String> muscleGroups) {
        this.name = name;
        this.description = description;
        this.muscleGroups = muscleGroups;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getMuscleGroups() { return muscleGroups; }
}