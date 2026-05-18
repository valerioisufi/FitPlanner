package com.example.fitplannerclient.entity.plan;

import com.example.fitplannerclient.controller.plan.AcceptWorkoutPlanVisitor;

public abstract class PlanNode implements AcceptWorkoutPlanVisitor {
    private String id;

    public abstract void execute();
    public abstract void reset();
}
