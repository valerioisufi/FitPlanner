package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.controller.plan.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;

public class ProgressionDecorator extends FlowDecorator {
    public ProgressionDecorator(PlanNode wrappedNode) {
        super(wrappedNode);
    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void execute() {

    }

    @Override
    public void reset() {

    }
}
