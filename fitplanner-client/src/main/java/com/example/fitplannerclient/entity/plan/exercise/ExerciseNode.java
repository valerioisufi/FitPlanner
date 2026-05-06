package com.example.fitplannerclient.entity.plan.exercise;

import com.example.fitplannerclient.entity.plan.PlanNode;

import java.util.List;

public class ExerciseNode extends PlanNode {
    private String resourceUuid;
    private List<ExerciseModifier> modifiers;

    @Override
    public void accept() {
        // Implementation for visitor pattern if needed
    }

    @Override
    public void execute() {
        // Implementation for executing the exercise
    }

    @Override
    public void reset() {
        // Implementation for resetting exercise state
    }
}


