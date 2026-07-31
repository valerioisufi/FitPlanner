package com.example.fitplannerclient.entity.plan;

import com.example.fitplannerclient.entity.plan.block.strategy.validation.ValidationResult;
import com.example.fitplannerclient.entity.plan.visitor.AcceptWorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.execution.ExecutionContext;
import com.example.fitplannerclient.entity.plan.execution.ExecutionResult;
import com.example.fitplannerclient.entity.plan.execution.PlanNodeState;
import com.example.fitplannerclient.util.IDGenerator;

import java.util.Optional;
import java.util.Set;

public abstract class PlanNode implements AcceptWorkoutPlanVisitor {
    private final String id = IDGenerator.generateUUID();
    protected PlanNodeState state = PlanNodeState.IDLE;

    public String getId() {
        return id;
    }
    public Optional<String> getName() {
        return Optional.empty();
    }

    public PlanNodeState getState() {
        return state;
    }

    public ValidationResult validate() {
        return new ValidationResult();
    }

    public Set<String> getExposedVariables() {
        return Set.of();
    }

    public abstract ExecutionResult execute(ExecutionContext context);
    public abstract void reset();
    public abstract PlanNode deepCopy();
}
