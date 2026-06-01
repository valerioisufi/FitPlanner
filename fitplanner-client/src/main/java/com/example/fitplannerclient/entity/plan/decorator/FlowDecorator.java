package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.entity.plan.PlanNode;

public abstract class FlowDecorator extends PlanNode{
    protected PlanNode wrappedNode;

    protected FlowDecorator(PlanNode wrappedNode) {
        this.wrappedNode = wrappedNode;
    }

    public PlanNode getWrappedNode(){
        return wrappedNode;
    }

    public void setWrappedNode(PlanNode wrappedNode){
        this.wrappedNode = wrappedNode;
    }

    public abstract FlowDecorator cloneWithNode(PlanNode newWrappedNode);

    @Override
    public PlanNode deepCopy() {
        return cloneWithNode(this.wrappedNode != null ? this.wrappedNode.deepCopy() : null);
    }
}
