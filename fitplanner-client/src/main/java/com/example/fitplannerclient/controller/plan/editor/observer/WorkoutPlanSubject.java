package com.example.fitplannerclient.controller.plan.editor.observer;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanSubject {
    private final List<WorkoutPlanObserver> observers = new ArrayList<>();

    public void attach(WorkoutPlanObserver observer) {
        this.observers.add(observer);
    }

    public void detach(WorkoutPlanObserver observer) {
        this.observers.remove(observer);
    }

    public void notifyObservers() {
        for (WorkoutPlanObserver observer : observers) {
            observer.update();
        }
    }

}
