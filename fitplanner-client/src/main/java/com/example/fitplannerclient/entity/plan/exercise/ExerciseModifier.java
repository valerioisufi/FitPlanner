package com.example.fitplannerclient.entity.plan.exercise;

public class ExerciseModifier {
    private final String name;
    private final String value;

    public ExerciseModifier(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public ExerciseModifier(ExerciseModifier other) {
        this.name = other.name;
        this.value = other.value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
