package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.visitor.WorkoutPlanVisitor;

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

    public abstract void setValue(String value);
    public abstract FlowDecoratorType getType();
    public abstract String getSerializedValue();

    public abstract FlowDecorator cloneWithNode(PlanNode newWrappedNode);

    @Override
    public PlanNode deepCopy() {
        return cloneWithNode(this.wrappedNode != null ? this.wrappedNode.deepCopy() : null);
    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }
}
