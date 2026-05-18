package com.example.fitplannerclient.controller.plan;

public interface AcceptWorkoutPlanVisitor {
    void accept(WorkoutPlanVisitor visitor);
}
