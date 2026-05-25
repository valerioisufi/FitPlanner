package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.entity.plan.PlanNode;

public abstract class FlowDecorator extends PlanNode{
    protected PlanNode wrappedNode;

    public FlowDecorator(PlanNode wrappedNode) {
        this.wrappedNode = wrappedNode;
    }

    public PlanNode getWrappedNode(){
        return wrappedNode;
    }

    public abstract FlowDecorator cloneWithNode(PlanNode newWrappedNode);
}
