package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.entity.plan.PlanNode;

public class RestDecorator extends FlowDecorator{
    private int restDuration;

    public RestDecorator(PlanNode wrappedNode, int restDuration) {
        super(wrappedNode);
        this.restDuration = restDuration;
    }

    @Override
    public void accept() {
        wrappedNode.accept();
    }

    @Override
    public void execute() {
        wrappedNode.execute();
    }

    @Override
    public void reset() {
        wrappedNode.reset();
    }


}
