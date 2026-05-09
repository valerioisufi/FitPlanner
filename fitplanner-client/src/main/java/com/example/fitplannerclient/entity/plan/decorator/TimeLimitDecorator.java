package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.entity.plan.PlanNode;

public class TimeLimitDecorator extends FlowDecorator {
    public TimeLimitDecorator(PlanNode wrappedNode) {
        super(wrappedNode);
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
