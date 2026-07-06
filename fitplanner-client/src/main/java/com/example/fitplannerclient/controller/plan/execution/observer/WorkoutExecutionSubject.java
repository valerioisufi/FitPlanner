package com.example.fitplannerclient.controller.plan.execution.observer;

import com.example.fitplannerclient.bean.exercise.CurrentExerciseBean;

import java.util.ArrayList;
import java.util.List;

public class WorkoutExecutionSubject {
    private final List<WorkoutExecutionObserver> observers = new ArrayList<>();

    public void attach(WorkoutExecutionObserver observer) {
        this.observers.add(observer);
    }

    public void detach(WorkoutExecutionObserver observer) {
        this.observers.remove(observer);
    }

    public void notifyCurrentExercise(CurrentExerciseBean currentExercise) {
        for (WorkoutExecutionObserver observer : observers) {
            observer.updateCurrentExercise(currentExercise);
        }
    }

    public void notifyCurrentWorkoutEngineState(WorkoutExecutionObserver.WorkoutExecutionState state) {
        for (WorkoutExecutionObserver observer : observers) {
            observer.updateCurrentWorkoutEngineState(state);
        }
    }

    public void notifyExecutionPhase(WorkoutExecutionObserver.WorkoutExecutionPhase phase) {
        for (WorkoutExecutionObserver observer : observers) {
            observer.updateExecutionPhase(phase);
        }
    }

    public void notifyCurrentRestTime(int restTimeSeconds) {
        for (WorkoutExecutionObserver observer : observers) {
            observer.updateCurrentRestTime(restTimeSeconds);
        }
    }
}
