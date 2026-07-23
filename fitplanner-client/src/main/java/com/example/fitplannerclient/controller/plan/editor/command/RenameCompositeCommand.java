package com.example.fitplannerclient.controller.plan.editor.command;

import com.example.fitplannerclient.entity.plan.block.CompositeNode;

public class RenameCompositeCommand implements WorkoutPlanEditorCommand {

    private CompositeNode compositeNode;
    private String newName;
    private String oldName;

    public RenameCompositeCommand(CompositeNode compositeNode, String newName) {
        this.compositeNode = compositeNode;

        this.newName = newName;
    }

    @Override
    public void execute() {
        oldName = compositeNode.getName().orElse(null);
        compositeNode.setName(newName);
    }

    @Override
    public void undo() {
        compositeNode.setName(oldName);
    }

}
