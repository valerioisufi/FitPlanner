package com.example.fitplannerclient.controller.plan.execution.engine.state;

import com.example.fitplannerclient.controller.plan.execution.engine.WorkoutEngineImpl;
import com.example.fitplannerclient.entity.plan.execution.WorkoutStatus;

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
