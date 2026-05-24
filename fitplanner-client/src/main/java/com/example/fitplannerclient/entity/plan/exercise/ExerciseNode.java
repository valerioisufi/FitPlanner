package com.example.fitplannerclient.entity.plan.exercise;

import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.context.ControlSignal;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;

import java.util.List;

public class ExerciseNode extends PlanNode {
    private String resourceId;
    private List<ExerciseModifier> modifiers;

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        if (context.consumeSignal(ControlSignal.SKIP_NEXT)) {
            this.state = PlanNodeState.SKIPPED;
            return new ExecutionResult(PlanNodeState.SKIPPED);
        }
        else if (context.consumeSignal(ControlSignal.SKIP_PREVIOUS)) {
            this.state = PlanNodeState.IDLE;
            return new ExecutionResult(PlanNodeState.REVERT);
        }

        if (this.state == PlanNodeState.IDLE) {
            // l'esercizio è iniziato
            this.state = PlanNodeState.RUNNING;
            return new ExecutionResult(PlanNodeState.RUNNING);
        } else if (this.state == PlanNodeState.RUNNING) {

            if (context.consumeSignal(ControlSignal.DONE)) {
                // l'esercizio è stato contrassegnato come completato
                this.state = PlanNodeState.COMPLETED;
                return new ExecutionResult(PlanNodeState.COMPLETED);
            }

            return new ExecutionResult(PlanNodeState.RUNNING);
        } else {
            return new ExecutionResult(this.state);
        }
    }

    @Override
    public void reset() {
        this.state = PlanNodeState.IDLE;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public List<ExerciseModifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<ExerciseModifier> modifiers) {
        this.modifiers = modifiers;
    }

}


