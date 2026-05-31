package com.example.fitplannerclient.controller.plan.execution.engine;

import com.example.fitplannerclient.entity.plan.context.WorkoutStatus;

public abstract class EngineState {

    public abstract WorkoutStatus getStatus();

    public boolean isPlaying() {
        return false;
    }

    public void entry(WorkoutEngineImpl engine){}

    public void exit(WorkoutEngineImpl engine){}


    public void play(WorkoutEngineImpl engine){}

    public void pause(WorkoutEngineImpl engine){}

    public void stop(WorkoutEngineImpl engine){}

    public void skipNext(WorkoutEngineImpl engine){}

    public void skipPrevious(WorkoutEngineImpl engine){}

    public void done(WorkoutEngineImpl engine){}

    public static EngineState getInitialState() {
        return new StopState();
    }

}
