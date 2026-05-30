package com.example.fitplannerclient.entity.plan;

import com.example.fitplannerclient.controller.plan.visitor.AcceptWorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;
import com.example.fitplannerclient.util.IDGenerator;

public abstract class PlanNode implements AcceptWorkoutPlanVisitor {
    private final String id = IDGenerator.generateUUID();
    protected PlanNodeState state = PlanNodeState.IDLE;

    public String getId() {
        return id;
    }

    public PlanNodeState getState() {
        return state;
    }

    public abstract ExecutionResult execute(ExecutionContext context);
    public abstract void reset();
    public abstract PlanNode deepCopy();
}
