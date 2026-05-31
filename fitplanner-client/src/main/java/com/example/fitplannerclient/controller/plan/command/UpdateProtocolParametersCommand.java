package com.example.fitplannerclient.controller.plan.command;

import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;

import java.util.HashMap;
import java.util.Map;

public class UpdateProtocolParametersCommand implements WorkoutPlanEditorCommand {

    private final ProtocolBlock block;
    private final Map<String, String> newParams;
    private final Map<String, String> oldParams;

    public UpdateProtocolParametersCommand(ProtocolBlock block, Map<String, String> newParams) {
        this.block = block;
        this.newParams = new HashMap<>(newParams);
        this.oldParams = new HashMap<>(block.getParameters());
    }

    @Override
    public void execute() {
        for (Map.Entry<String, String> entry : newParams.entrySet()) {
            block.setParameter(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void undo() {
        for (Map.Entry<String, String> entry : oldParams.entrySet()) {
            block.setParameter(entry.getKey(), entry.getValue());
        }
    }
}
