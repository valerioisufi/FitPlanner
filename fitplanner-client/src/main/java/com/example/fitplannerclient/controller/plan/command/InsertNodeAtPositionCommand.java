package com.example.fitplannerclient.controller.plan.command;

import com.example.fitplannerclient.controller.plan.EditWorkoutPlanManager;

public class InsertNodeAtPositionCommand implements WorkoutPlanEditorCommand {
    private EditWorkoutPlanManager manager;

    private String nodeId;
    private String parentId;
    private int position;

    public InsertNodeAtPositionCommand(EditWorkoutPlanManager manager, String nodeId, String parentId, int position) {
        this.manager = manager;

        this.nodeId = nodeId;
        this.parentId = parentId;
        this.position = position;
    }

    @Override
    public void execute() {

    }

    @Override
    public void undo() {

    }
}
