package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.entity.plan.PlanNode;

public class LoopDecorator extends FlowDecorator {
    private int rounds;

    public LoopDecorator(PlanNode wrappedNode, int rounds) {
        super(wrappedNode);
        this.rounds = rounds;
    }

    @Override
    public void accept() {

    }

    @Override
    public void execute() {

    }

    @Override
    public void reset() {

    }
}
