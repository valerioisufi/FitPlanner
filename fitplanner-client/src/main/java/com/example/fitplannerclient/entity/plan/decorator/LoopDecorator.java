package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.controller.plan.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;

public class LoopDecorator extends FlowDecorator {
    private int rounds;

    public LoopDecorator(PlanNode wrappedNode, int rounds) {
        super(wrappedNode);
        this.rounds = rounds;
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
