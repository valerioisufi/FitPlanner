package com.example.fitplannerclient.controller.plan.editor.command;

import com.example.fitplannerclient.entity.plan.block.Block;

public class RenameBlockCommand implements WorkoutPlanEditorCommand {

    private Block block;
    private String newName;
    private String oldName;

    public RenameBlockCommand(Block block, String newName) {
        this.block = block;

        this.newName = newName;
    }

    @Override
    public void execute() {
        oldName = block.getTitle();
        block.setTitle(newName);
    }

    @Override
    public void undo() {
        block.setTitle(oldName);
    }

}
