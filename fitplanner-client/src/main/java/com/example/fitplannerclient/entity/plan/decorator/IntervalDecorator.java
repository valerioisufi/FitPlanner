package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.controller.plan.core.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.context.ControlSignal;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;

public class IntervalDecorator extends FlowDecorator {
    private String intervalDuration;
    private int timeLeftMillis = 0;

    public IntervalDecorator(PlanNode wrappedNode, String intervalDuration) {
        super(wrappedNode);
        this.intervalDuration = intervalDuration;
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
            this.timeLeftMillis = context.resolveAsInteger(intervalDuration, 0);
        }

        int delta = context.getTickDelta();

        if (this.state == PlanNodeState.RUNNING) {
            return handleRunningState(context, delta);
        } else if (this.state == PlanNodeState.WAITING) {
            return handleWaitingState(context, delta);
        }

        return new ExecutionResult(this.state);
    }

    private ExecutionResult handleRunningState(ExecutionContext context, int delta) {
        int consumed = Math.min(this.timeLeftMillis, delta);
        this.timeLeftMillis -= consumed;

        if (this.timeLeftMillis == 0) {
            this.state = PlanNodeState.COMPLETED;
            context.consumeTickDelta(consumed);

            return new ExecutionResult(PlanNodeState.COMPLETED);
        }

        ExecutionResult result = wrappedNode.execute(context);

        if (result.getState() == PlanNodeState.COMPLETED || result.getState() == PlanNodeState.SKIPPED) {
            // il figlio ha finito prima del tempo dell'intervallo
            this.state = PlanNodeState.WAITING;
            return new ExecutionResult(PlanNodeState.WAITING, this.timeLeftMillis);

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

    private ExecutionResult handleWaitingState(ExecutionContext context, int delta) {
        if (context.consumeSignal(ControlSignal.SKIP_NEXT)) {
            // l'utente salta il resto del riposo
            context.consumeTickDelta(context.getTickDelta());

            this.state = PlanNodeState.COMPLETED;
            return new ExecutionResult(PlanNodeState.COMPLETED);
        }
        if (context.consumeSignal(ControlSignal.SKIP_PREVIOUS)) {
            context.consumeTickDelta(context.getTickDelta());

            this.reset();
            return new ExecutionResult(PlanNodeState.REVERT);
        }

        int consumed = Math.min(this.timeLeftMillis, delta);
        this.timeLeftMillis -= consumed;
        context.consumeTickDelta(consumed);

        if (this.timeLeftMillis == 0) {
            this.state = PlanNodeState.COMPLETED;
            return new ExecutionResult(PlanNodeState.COMPLETED);
        } else {
            return new ExecutionResult(PlanNodeState.WAITING, this.timeLeftMillis);
        }
    }

    @Override
    public void reset() {
        this.state = PlanNodeState.IDLE;
        this.timeLeftMillis = 0;

        this.wrappedNode.reset();
    }

    public String getIntervalDuration() {
        return intervalDuration;
    }

    public void setIntervalDuration(String intervalDuration) {
        this.intervalDuration = intervalDuration;
    }

    @Override
    public IntervalDecorator cloneWithNode(PlanNode newWrappedNode) {
        return new IntervalDecorator(newWrappedNode, this.intervalDuration);
    }
}
