package com.example.fitplannerclient.controller.plan.core.visitor;

public interface AcceptWorkoutPlanVisitor {
    void accept(WorkoutPlanVisitor visitor);
}
