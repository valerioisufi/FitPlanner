package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.entity.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.execution.ExecutionContext;
import com.example.fitplannerclient.entity.plan.execution.ExecutionResult;
import com.example.fitplannerclient.entity.plan.execution.PlanNodeState;

public class TimeLimitDecorator extends FlowDecorator {
    private String timeLimit; // espresso in secondi
    private int timeLeftMillis = 0;

    public TimeLimitDecorator(PlanNode wrappedNode, String timeLimit) {
        super(wrappedNode);
        this.timeLimit = timeLimit;
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
            this.timeLeftMillis = context.resolveAsInteger(timeLimit, 0) * 1000;
        }

        int delta = context.getTickDelta();

        if (this.state == PlanNodeState.RUNNING) {
            int consumed = Math.min(this.timeLeftMillis, delta);
            this.timeLeftMillis -= consumed;

            if (this.timeLeftMillis == 0) {
                this.state = PlanNodeState.COMPLETED;
                context.consumeTickDelta(consumed);

                return new ExecutionResult(PlanNodeState.COMPLETED);
            }

            ExecutionResult result = wrappedNode.execute(context);

            if (result.getState() == PlanNodeState.COMPLETED || result.getState() == PlanNodeState.SKIPPED) {
                this.state = PlanNodeState.COMPLETED;
                return new ExecutionResult(PlanNodeState.COMPLETED);

            } else if (result.getState() == PlanNodeState.REVERT) {
                this.reset();
                return new ExecutionResult(PlanNodeState.REVERT);

            } else if (result.getState() == PlanNodeState.RUNNING) {
                int childSleep = result.getRequestedSleepMillis();
                int sleepTime = (childSleep < 0) ? this.timeLeftMillis : Math.min(childSleep, this.timeLeftMillis);

                context.prependBreadcrumb("Time Limit " + (this.timeLeftMillis / 1000) + "s");
                return new ExecutionResult(PlanNodeState.RUNNING, sleepTime);
            }
            if (result.getState() == PlanNodeState.WAITING) {
                context.prependBreadcrumb("Time Limit " + (this.timeLeftMillis / 1000) + "s");
            }

            return result;
        }

        return new ExecutionResult(this.state);
    }

    @Override
    public void reset() {
        this.state = PlanNodeState.IDLE;
        this.timeLeftMillis = 0;

        this.wrappedNode.reset();
    }

    public String getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(String timeLimit) {
        this.timeLimit = timeLimit;
    }

    @Override
    public TimeLimitDecorator cloneWithNode(PlanNode newWrappedNode) {
        return new TimeLimitDecorator(newWrappedNode, this.timeLimit);
    }
}
