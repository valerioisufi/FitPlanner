package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.entity.plan.PlanNode;

public class ProgressionDecorator extends FlowDecorator {
    public ProgressionDecorator(PlanNode wrappedNode) {
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
