package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;

public class LoopDecorator extends FlowDecorator {
    private int rounds;

    private int currentRound = 0;

    public LoopDecorator(PlanNode wrappedNode, int rounds) {
        super(wrappedNode);
        this.rounds = rounds;
    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        if(currentRound <= rounds) {
            ExecutionResult result = wrappedNode.execute(context);
            if(result.getState() == PlanNodeState.COMPLETED) {
                currentRound++;
                wrappedNode.reset();
            }

            return new ExecutionResult(PlanNodeState.RUNNING, result.getRequestedSleepMillis());
        } else {
            return new ExecutionResult(PlanNodeState.COMPLETED, 0);
        }

    }

    @Override
    public void reset() {
        currentRound = 0;
        wrappedNode.reset();
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }
}
