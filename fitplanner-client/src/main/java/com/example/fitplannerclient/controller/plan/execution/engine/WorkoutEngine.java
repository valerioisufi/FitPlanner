package com.example.fitplannerclient.controller.plan.execution.engine;

import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

public interface WorkoutEngine {
    void play();
    void pause();
    void stop();

    void skipPrevious();
    void skipNext();
    void done();

    @FunctionalInterface
    interface UpdateCallback {
        void onUpdate(EngineState state, ExerciseNode activeNode, int timeRemainingMillis);
    }

    void setOnUpdateListener(UpdateCallback callback);
    EngineState getState();
}
