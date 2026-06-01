package com.example.fitplannerclient.mock;

import com.example.fitplannerclient.controller.plan.core.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;

public class DummyPlanNode extends PlanNode {
    private int executeCallCount = 0;
    private int resetCallCount = 0;

    private ExecutionResult resultToReturn = new ExecutionResult(PlanNodeState.RUNNING);

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        executeCallCount++;
        return resultToReturn;
    }

    @Override
    public void reset() {
        resetCallCount++;
        resultToReturn = new ExecutionResult(PlanNodeState.RUNNING);
    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        // Metodo vuoto intenzionalmente nel mock per i test
    }

    public int getExecuteCallCount() {
        return executeCallCount;
    }

    public int getResetCallCount() {
        return resetCallCount;
    }

    public void setNextResult(PlanNodeState state) {
        this.resultToReturn = new ExecutionResult(state);
    }

    public void setNextResult(PlanNodeState state, int requestedSleepMillis) {
        this.resultToReturn = new ExecutionResult(state, requestedSleepMillis);
    }
}
