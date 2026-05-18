package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.controller.plan.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;

public class IntervalDecorator extends FlowDecorator{
    public IntervalDecorator(PlanNode wrappedNode) {
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
