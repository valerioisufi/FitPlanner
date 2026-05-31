package com.example.fitplannerclient.controller.plan.execution.engine;

import com.example.fitplannerclient.entity.plan.context.WorkoutStatus;

public class StopState extends EngineState {

    @Override
    public WorkoutStatus getStatus() {
        return WorkoutStatus.STOPPED;
    }

    @Override
    public void entry(WorkoutEngineImpl engine) {
        engine.reset();
    }

    @Override
    public void play(WorkoutEngineImpl engine) {
        engine.changeToState(new PlayState());
    }

}
