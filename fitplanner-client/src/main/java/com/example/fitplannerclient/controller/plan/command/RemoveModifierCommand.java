package com.example.fitplannerclient.controller.plan.command;

import com.example.fitplannerclient.entity.plan.exercise.ExerciseModifier;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import com.example.fitplannerclient.entity.plan.exercise.ModifierType;

public class RemoveModifierCommand implements WorkoutPlanEditorCommand {

    private ExerciseNode exerciseNode;
    private ModifierType type;
    private ExerciseModifier oldModifier;

    public RemoveModifierCommand(ExerciseNode exerciseNode, ModifierType type) {
        this.exerciseNode = exerciseNode;
        this.type = type;
    }

    @Override
    public void execute() {
        oldModifier = exerciseNode.removeModifier(type);
    }

    @Override
    public void undo() {
        if (oldModifier != null) {
            exerciseNode.addModifier(oldModifier);
        }
    }
}
