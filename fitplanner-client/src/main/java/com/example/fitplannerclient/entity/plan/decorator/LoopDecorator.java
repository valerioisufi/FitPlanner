package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;

public class LoopDecorator extends FlowDecorator {
    private int rounds;

    protected int currentRound = 0;

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
        if (this.state == PlanNodeState.COMPLETED) {
            return new ExecutionResult(PlanNodeState.COMPLETED);
        }

        this.state = PlanNodeState.RUNNING;

        ExecutionResult result = wrappedNode.execute(context);

        if (result.getState() == PlanNodeState.COMPLETED || result.getState() == PlanNodeState.SKIPPED) {
            currentRound++;

            if (currentRound < rounds) {
                wrappedNode.reset();
                return this.execute(context);

            } else {
                // ho terminato tutti i round
                this.state = PlanNodeState.COMPLETED;
                return new ExecutionResult(PlanNodeState.COMPLETED);
            }
        } else if (result.getState() == PlanNodeState.REVERT) {
            if(currentRound > 0) {
                currentRound = currentRound - 1;
                wrappedNode.reset();
                return this.execute(context);
            } else {
                wrappedNode.reset();
                return new ExecutionResult(PlanNodeState.REVERT);

            }
        }

        // se il figlio è RUNNING o WAITING, lo lascio passare
        return result;

    }

    @Override
    public void reset() {
        this.state = PlanNodeState.IDLE;
        currentRound = 0;

        wrappedNode.reset();
    }

    public int getRounds() {
        return rounds;
    }

    public void setRounds(int rounds) {
        this.rounds = rounds;
    }

    @Override
    public LoopDecorator cloneWithNode(PlanNode newWrappedNode) {
        return new LoopDecorator(newWrappedNode, this.rounds);
    }
}
