package com.example.fitplannerclient.controller.plan.execution.engine;

import com.example.fitplannerclient.controller.plan.execution.engine.state.EngineState;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.execution.ExecutionContext;
import com.example.fitplannerclient.entity.plan.execution.ExecutionResult;
import com.example.fitplannerclient.entity.plan.execution.WorkoutStatus;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

public class WorkoutEngineImpl implements WorkoutEngine {

    private EngineState currentState;
    private PlanNode planRoot;
    private ExecutionContext context;
    private UpdateCallback updateListener;

    public WorkoutEngineImpl(PlanNode planRoot) {
        this.planRoot = planRoot;
        this.context = new ExecutionContext();
        this.currentState = EngineState.getInitialState();
        this.currentState.entry(this);
    }

    public void changeToState(EngineState newState) {
        currentState.exit(this);
        currentState = newState;
        currentState.entry(this);
    }

    @Override
    public void play() {
        currentState.play(this);
    }

    @Override
    public void pause() {
        currentState.pause(this);
    }

    @Override
    public void stop() {
        currentState.stop(this);
    }

    @Override
    public void skipPrevious() {
        currentState.skipPrevious(this);
    }

    @Override
    public void skipNext() {
        currentState.skipNext(this);
    }

    @Override
    public void done() {
        currentState.done(this);
    }

    @Override
    public void setOnUpdateListener(UpdateCallback callback) {
        this.updateListener = callback;
    }

    public void notifyUpdate(WorkoutStatus status, ExecutionResult result, ExerciseNode activeNode) {
        if (updateListener != null) {
            updateListener.onUpdate(status, result, activeNode);
        }
    }

    public ExecutionContext getContext() {
        return context;
    }

    public ExecutionResult execute(ExecutionContext context) {
        context.clearBreadcrumb();
        return planRoot.execute(context);
    }

    public void reset() {
        planRoot.reset();
        context.reset();
    }

    @Override
    public EngineState getState() {
        return currentState;
    }
}
