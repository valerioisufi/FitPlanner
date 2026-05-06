package com.example.fitplannerclient.entity.plan.exercise;

public class ExerciseModifier {
    private String name;
    private String value;

    public ExerciseModifier(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
