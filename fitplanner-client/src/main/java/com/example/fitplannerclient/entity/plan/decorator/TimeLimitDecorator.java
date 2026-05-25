package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;

public class TimeLimitDecorator extends FlowDecorator {
    private int timeLimitMillis;
    private int timeLeftMillis = 0;

    public TimeLimitDecorator(PlanNode wrappedNode, int timeLimitMillis) {
        super(wrappedNode);
        this.timeLimitMillis = timeLimitMillis;
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
            this.timeLeftMillis = timeLimitMillis;
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

                return new ExecutionResult(PlanNodeState.RUNNING, sleepTime);
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

    public int getTimeLimitMillis() {
        return timeLimitMillis;
    }

    public void setTimeLimitMillis(int timeLimitMillis) {
        this.timeLimitMillis = timeLimitMillis;
    }
}
