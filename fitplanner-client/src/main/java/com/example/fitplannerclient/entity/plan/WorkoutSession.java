package com.example.fitplannerclient.entity.plan;

import com.example.fitplannerclient.controller.plan.visitor.AcceptWorkoutPlanVisitor;
import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;

public class WorkoutSession implements AcceptWorkoutPlanVisitor {
    private String name;
    private int day;

    private PlanNode root;

    public WorkoutSession(String name, int day, PlanNode root) {
        this.name = name;
        this.day = day;
        this.root = root;
    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }


    public String getName(){
        return this.name;
    }

    public int getDay(){
        return this.day;
    }

    public PlanNode getRoot(){
        return this.root;
    }


}
