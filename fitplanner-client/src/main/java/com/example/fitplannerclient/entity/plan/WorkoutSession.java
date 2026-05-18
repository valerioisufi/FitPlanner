package com.example.fitplannerclient.entity.plan;

import com.example.fitplannerclient.controller.plan.AcceptWorkoutPlanVisitor;
import com.example.fitplannerclient.controller.plan.WorkoutPlanVisitor;

public class WorkoutSession implements AcceptWorkoutPlanVisitor {
    private String name;
    private String id;

    private int day;

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    public String getId(){
        return this.id;
    }


}
