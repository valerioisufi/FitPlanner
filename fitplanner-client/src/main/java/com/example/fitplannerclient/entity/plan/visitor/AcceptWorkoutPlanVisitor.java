package com.example.fitplannerclient.entity.plan.visitor;

public interface AcceptWorkoutPlanVisitor {
    void accept(WorkoutPlanVisitor visitor);
}
