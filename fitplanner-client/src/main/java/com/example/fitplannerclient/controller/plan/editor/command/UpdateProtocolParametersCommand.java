package com.example.fitplannerclient.controller.plan.editor.command;

import com.example.fitplannerclient.entity.plan.block.CompositeNode;

import java.util.HashMap;
import java.util.Map;

public class UpdateProtocolParametersCommand implements WorkoutPlanEditorCommand {

    private final CompositeNode compositeNode;
    private final Map<String, String> newParams;
    private final Map<String, String> oldParams;

    public UpdateProtocolParametersCommand(CompositeNode compositeNode, Map<String, String> newParams) {
        this.compositeNode = compositeNode;
        this.newParams = new HashMap<>(newParams);
        this.oldParams = new HashMap<>(compositeNode.getParameters());
    }

    @Override
    public void execute() {
        for (Map.Entry<String, String> entry : newParams.entrySet()) {
            compositeNode.setParameter(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void undo() {
        for (Map.Entry<String, String> entry : oldParams.entrySet()) {
            compositeNode.setParameter(entry.getKey(), entry.getValue());
        }
    }
}
