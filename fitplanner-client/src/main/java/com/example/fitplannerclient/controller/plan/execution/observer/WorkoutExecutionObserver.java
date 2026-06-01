package com.example.fitplannerclient.controller.plan.execution.observer;

import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;

public interface WorkoutExecutionObserver {

    void updateCurrentExercise(ExerciseDescriptionBean description);

    void updateCurrentWorkoutEngineState(WorkoutExecutionState state);

    void updateCurrentRestTime(int restTimeSeconds);

    enum WorkoutExecutionState {
        STOPPED,
        PLAYING,
        PAUSED
    }
}
