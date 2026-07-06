package com.example.fitplannerclient.controller.plan.execution.engine.state;

import com.example.fitplannerclient.controller.plan.execution.engine.WorkoutEngineImpl;
import com.example.fitplannerclient.entity.plan.execution.ControlSignal;
import com.example.fitplannerclient.entity.plan.execution.ExecutionContext;
import com.example.fitplannerclient.entity.plan.execution.ExecutionResult;
import com.example.fitplannerclient.entity.plan.execution.PlanNodeState;
import com.example.fitplannerclient.entity.plan.execution.WorkoutStatus;

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
        executeWithSignal(engine, ControlSignal.SKIP_NEXT);
    }

    @Override
    public void skipPrevious(WorkoutEngineImpl engine) {
        executeWithSignal(engine, ControlSignal.SKIP_PREVIOUS);
    }

    @Override
    public void done(WorkoutEngineImpl engine) {
        executeWithSignal(engine, ControlSignal.DONE);
    }

    private void executeWithSignal(WorkoutEngineImpl engine, ControlSignal signal) {
        ExecutionContext context = engine.getContext();
        context.injectSignal(signal);

        ExecutionResult result = engine.execute(context);
        engine.notifyUpdate(this.getStatus(), result, context.getActiveNode(), context.getBreadcrumb());

        if (result.getState() == PlanNodeState.COMPLETED) {
            // il piano è terminato mentre il motore era in pausa
            stop(engine);
        }
    }
}
