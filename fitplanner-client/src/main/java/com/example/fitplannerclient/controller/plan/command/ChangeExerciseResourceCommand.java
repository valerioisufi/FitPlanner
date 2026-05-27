package com.example.fitplannerclient.controller.plan.command;

import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

public class ChangeExerciseResourceCommand implements WorkoutPlanEditorCommand {

    private ExerciseNode exerciseNode;
    private String newResourceId;
    private String oldResourceId;

    public ChangeExerciseResourceCommand(ExerciseNode exerciseNode, String newResourceId) {
        this.exerciseNode = exerciseNode;
        this.newResourceId = newResourceId;
    }

    @Override
    public void execute() {
        oldResourceId = exerciseNode.getResourceId();
        exerciseNode.setResourceId(newResourceId);
    }

    @Override
    public void undo() {
        exerciseNode.setResourceId(oldResourceId);
    }
}
