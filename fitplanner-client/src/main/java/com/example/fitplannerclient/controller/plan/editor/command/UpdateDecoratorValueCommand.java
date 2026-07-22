package com.example.fitplannerclient.controller.plan.editor.command;

import com.example.fitplannerclient.entity.plan.visitor.EmptyWorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.decorator.*;

public class UpdateDecoratorValueCommand implements WorkoutPlanEditorCommand {

    private FlowDecorator flowDecorator;

    private String previousValue;
    private String newValue;

    public UpdateDecoratorValueCommand(FlowDecorator flowDecorator, String newValue) {
        this.flowDecorator = flowDecorator;
        this.newValue = newValue;
    }

    @Override
    public void execute() {
        previousValue = flowDecorator.getSerializedValue();
        flowDecorator.setValue(newValue);
    }

    @Override
    public void undo() {
        flowDecorator.setValue(previousValue);
    }
}
