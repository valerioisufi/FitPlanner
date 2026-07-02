package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.entity.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.execution.ExecutionContext;
import com.example.fitplannerclient.entity.plan.execution.ExecutionResult;
import com.example.fitplannerclient.entity.plan.execution.PlanNodeState;

public class LoopDecorator extends FlowDecorator {
    private String roundsExpression;

    protected int currentRound = 0;
    protected int resolvedRounds = 1;

    public LoopDecorator(PlanNode wrappedNode, String roundsExpression) {
        super(wrappedNode);
        this.roundsExpression = roundsExpression;
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

        if (this.state == PlanNodeState.IDLE) {
            this.state = PlanNodeState.RUNNING;
            this.resolvedRounds = context.resolveAsInteger(this.roundsExpression, 1);
        }

        ExecutionResult result = wrappedNode.execute(context);

        if (result.getState() == PlanNodeState.COMPLETED || result.getState() == PlanNodeState.SKIPPED) {
            currentRound++;

            if (currentRound < resolvedRounds) {
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

    public String getRoundsExpression() {
        return roundsExpression;
    }

    public void setRoundsExpression(String roundsExpression) {
        this.roundsExpression = roundsExpression;
    }

    @Override
    public LoopDecorator cloneWithNode(PlanNode newWrappedNode) {
        return new LoopDecorator(newWrappedNode, this.roundsExpression);
    }
}
