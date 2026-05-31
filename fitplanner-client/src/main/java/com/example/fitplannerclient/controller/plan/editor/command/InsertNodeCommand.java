package com.example.fitplannerclient.controller.plan.editor.command;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.block.GroupNode;

public class InsertNodeCommand implements WorkoutPlanEditorCommand {

    private PlanNode newNode;
    private GroupNode parentNode;
    private int index;

    public InsertNodeCommand(PlanNode newNode, GroupNode parentNode, int index) {
        this.newNode = newNode;
        this.parentNode = parentNode;
        this.index = index;
    }

    @Override
    public void execute() {
        parentNode.addNodeAt(index, newNode);
    }

    @Override
    public void undo() {
        parentNode.removeNodeAt(index);

    }
}
