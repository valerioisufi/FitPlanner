package com.example.fitplannerclient.controller.plan.execution.engine;

import com.example.fitplannerclient.controller.plan.execution.engine.state.EngineState;
import com.example.fitplannerclient.entity.plan.execution.ExecutionResult;
import com.example.fitplannerclient.entity.plan.execution.WorkoutStatus;
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
        void onUpdate(WorkoutStatus status, ExecutionResult result, ExerciseNode activeNode, String breadcrumb);
    }

    void setOnUpdateListener(UpdateCallback callback);
    EngineState getState();
}
