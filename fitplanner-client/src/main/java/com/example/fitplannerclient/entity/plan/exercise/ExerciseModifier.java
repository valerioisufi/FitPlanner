package com.example.fitplannerclient.entity.plan.exercise;

public class ExerciseModifier {
    private final ModifierType type;
    private final String value;

    public ExerciseModifier(ModifierType type, String value) {
        this.type = type;
        this.value = value;
    }

    public ExerciseModifier(ExerciseModifier other) {
        this.type = other.type;
        this.value = other.value;
    }

    public ModifierType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
