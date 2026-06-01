package com.example.fitplannerclient.controller.plan.execution.observer;

import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;

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

    public void notifyCurrentExercise(ExerciseDescriptionBean description) {
        for (WorkoutExecutionObserver observer : observers) {
            observer.updateCurrentExercise(description);
        }
    }

    public void notifyCurrentWorkoutEngineState(WorkoutExecutionObserver.WorkoutExecutionState state) {
        for (WorkoutExecutionObserver observer : observers) {
            observer.updateCurrentWorkoutEngineState(state);
        }
    }

    public void notifyCurrentRestTime(int restTimeSeconds) {
        for (WorkoutExecutionObserver observer : observers) {
            observer.updateCurrentRestTime(restTimeSeconds);
        }
    }
}
