package com.example.fitplannerclient.controller.plan.execution.observer;

import com.example.fitplannerclient.bean.exercise.CurrentExerciseBean;

public interface WorkoutExecutionObserver {

    void updateCurrentExercise(CurrentExerciseBean currentExercise);

    void updateCurrentWorkoutEngineState(WorkoutExecutionState state);

    void updateExecutionPhase(WorkoutExecutionPhase phase);

    void updateCurrentRestTime(int restTimeSeconds);

    enum WorkoutExecutionState {
        STOPPED,
        PLAYING,
        PAUSED
    }

    enum WorkoutExecutionPhase {
        EXERCISE,
        REST,
        COMPLETED
    }
}
