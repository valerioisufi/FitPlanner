package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.context.ControlSignal;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;

public class RestDecorator extends FlowDecorator{
    private int restDurationMillis;

    private int sleepTimeMillis = 0;

    public RestDecorator(PlanNode wrappedNode, int restDurationMillis) {
        super(wrappedNode);
        this.restDurationMillis = restDurationMillis;
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

        if (this.state == PlanNodeState.WAITING) {
            if (context.consumeSignal(ControlSignal.SKIP_NEXT)) {
                // l'utente vuole saltare il riposo
                context.consumeTickDelta(context.getTickDelta());

                this.state = PlanNodeState.COMPLETED;
                return new ExecutionResult(PlanNodeState.COMPLETED);
            }
            if (context.consumeSignal(ControlSignal.SKIP_PREVIOUS)) {
                // durante il riposto l'utente ha deciso di tornare all'esercizio precedente
                context.consumeTickDelta(context.getTickDelta());

                this.reset();
                return new ExecutionResult(PlanNodeState.REVERT);
            }

            int tickDelta = context.getTickDelta();
            int consumed = Math.min(this.sleepTimeMillis, tickDelta);

            this.sleepTimeMillis -= consumed;
            context.consumeTickDelta(consumed);

            if(this.sleepTimeMillis == 0) {
                this.state = PlanNodeState.COMPLETED;
                return new ExecutionResult(PlanNodeState.COMPLETED);
            } else {
                return new ExecutionResult(PlanNodeState.WAITING, this.sleepTimeMillis);
            }
        }

        this.state = PlanNodeState.RUNNING;
        ExecutionResult result = wrappedNode.execute(context);

        if (result.getState() == PlanNodeState.COMPLETED || result.getState() == PlanNodeState.SKIPPED) {
            if (restDurationMillis <= 0) {
                this.state = PlanNodeState.COMPLETED;
                return new ExecutionResult(PlanNodeState.COMPLETED);
            }

            // passo allo stato di riposo
            this.state = PlanNodeState.WAITING;
            this.sleepTimeMillis = restDurationMillis;

            return new ExecutionResult(PlanNodeState.WAITING, restDurationMillis);
        } else if (result.getState() == PlanNodeState.REVERT) {
            this.reset();

            return new ExecutionResult(PlanNodeState.REVERT);
        }

        return result;
    }

    @Override
    public void reset() {
        this.state = PlanNodeState.IDLE;
        this.sleepTimeMillis = 0;

        wrappedNode.reset();
    }

    public int getRestDuration() {
        return restDurationMillis;
    }

    public void setRestDuration(int restDurationMillis) {
        this.restDurationMillis = restDurationMillis;
    }


}
