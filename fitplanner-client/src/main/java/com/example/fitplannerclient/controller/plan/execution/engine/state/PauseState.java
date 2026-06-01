package com.example.fitplannerclient.controller.plan.execution.engine.state;

import com.example.fitplannerclient.controller.plan.execution.engine.WorkoutEngineImpl;
import com.example.fitplannerclient.entity.plan.context.ControlSignal;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;

import com.example.fitplannerclient.entity.plan.context.WorkoutStatus;

public class PauseState extends EngineState {

    @Override
    public WorkoutStatus getStatus() {
        return WorkoutStatus.PAUSED;
    }

    @Override
    public void play(WorkoutEngineImpl engine) {
        engine.changeToState(new PlayState());
    }

    @Override
    public void stop(WorkoutEngineImpl engine) {
        engine.changeToState(new StopState());
    }

    @Override
    public void skipNext(WorkoutEngineImpl engine) {
        ExecutionContext context = engine.getContext();
        context.injectSignal(ControlSignal.SKIP_NEXT);

        ExecutionResult result = engine.execute(context);
        engine.notifyUpdate(this, context.getActiveNode(), result.getRequestedSleepMillis());
    }

    @Override
    public void skipPrevious(WorkoutEngineImpl engine) {
        ExecutionContext context = engine.getContext();
        context.injectSignal(ControlSignal.SKIP_PREVIOUS);

        ExecutionResult result = engine.execute(context);
        engine.notifyUpdate(this, context.getActiveNode(), result.getRequestedSleepMillis());
    }

    @Override
    public void done(WorkoutEngineImpl engine) {
        ExecutionContext context = engine.getContext();
        context.injectSignal(ControlSignal.DONE);

        ExecutionResult result = engine.execute(context);
        engine.notifyUpdate(this, context.getActiveNode(), result.getRequestedSleepMillis());
    }
}
