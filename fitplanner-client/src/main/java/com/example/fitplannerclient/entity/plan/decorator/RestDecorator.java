package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;

public class RestDecorator extends FlowDecorator{
    private int restDuration;

    public RestDecorator(PlanNode wrappedNode, int restDuration) {
        super(wrappedNode);
        this.restDuration = restDuration;
    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        // TODO
        return null;
    }

    @Override
    public void reset() {
        this.state = PlanNodeState.IDLE;
        wrappedNode.reset();
    }

    public int getRestDuration() {
        return restDuration;
    }

    public void setRestDuration(int restDuration) {
        this.restDuration = restDuration;
    }


}
