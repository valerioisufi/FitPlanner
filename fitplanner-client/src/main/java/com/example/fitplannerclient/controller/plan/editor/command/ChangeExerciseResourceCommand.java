package com.example.fitplannerclient.controller.plan.editor.command;

import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

public class ChangeExerciseResourceCommand implements WorkoutPlanEditorCommand {

    private final ExerciseNode exerciseNode;
    private final String newExerciseId;
    private final String newExerciseName;

    private String oldExerciseId;
    private String oldExerciseName;

    public ChangeExerciseResourceCommand(ExerciseNode exerciseNode, String newExerciseId, String newExerciseName) {
        this.exerciseNode = exerciseNode;
        this.newExerciseId = newExerciseId;
        this.newExerciseName = newExerciseName;
    }

    @Override
    public void execute() {
        oldExerciseId = exerciseNode.getResourceId();
        oldExerciseName = exerciseNode.getName().orElse(null);
        exerciseNode.setExerciseInfo(newExerciseId, newExerciseName);
    }

    @Override
    public void undo() {
        exerciseNode.setExerciseInfo(oldExerciseId, oldExerciseName);
    }
}
