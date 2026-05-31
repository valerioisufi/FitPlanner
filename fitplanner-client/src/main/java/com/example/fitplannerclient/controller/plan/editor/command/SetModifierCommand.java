package com.example.fitplannerclient.controller.plan.editor.command;

import com.example.fitplannerclient.entity.plan.exercise.ExerciseModifier;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

public class SetModifierCommand implements WorkoutPlanEditorCommand {

    private ExerciseNode exerciseNode;
    private ExerciseModifier newModifier;
    private ExerciseModifier oldModifier;

    public SetModifierCommand(ExerciseNode exerciseNode, ExerciseModifier newModifier) {
        this.exerciseNode = exerciseNode;
        this.newModifier = newModifier;
    }

    @Override
    public void execute() {
        oldModifier = exerciseNode.getModifier(newModifier.getType());
        exerciseNode.addModifier(newModifier);
    }

    @Override
    public void undo() {
        if (oldModifier != null) {
            exerciseNode.addModifier(oldModifier);
        } else {
            exerciseNode.removeModifier(newModifier.getType());
        }
    }
}
