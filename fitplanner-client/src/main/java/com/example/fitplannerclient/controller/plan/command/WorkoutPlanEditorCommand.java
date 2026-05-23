package com.example.fitplannerclient.controller.plan.command;

public interface WorkoutPlanEditorCommand {
    void execute();

    void undo();
}
