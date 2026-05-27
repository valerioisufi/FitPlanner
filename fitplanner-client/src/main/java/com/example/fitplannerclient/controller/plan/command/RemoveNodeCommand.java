package com.example.fitplannerclient.controller.plan.command;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.block.GroupNode;

public class RemoveNodeCommand implements WorkoutPlanEditorCommand {

    private PlanNode oldNode;
    private GroupNode parent;
    private int index;

    public RemoveNodeCommand(GroupNode parent, int index) {
        this.parent = parent;
        this.index = index;
    }

    @Override
    public void execute() {
        oldNode = parent.removeNodeAt(index);
    }

    @Override
    public void undo() {
        parent.addNodeAt(index, oldNode);
    }
}
