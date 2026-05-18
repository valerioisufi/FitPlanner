package com.example.fitplannerclient.entity.plan.block;

import com.example.fitplannerclient.controller.plan.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;

public class Block extends GroupNode{
    private String title;

    @Override
    public void addNodeAt(int index, PlanNode node) {

    }

    @Override
    public void replaceNode() {

    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void execute() {

    }

    @Override
    public void reset() {

    }
}
