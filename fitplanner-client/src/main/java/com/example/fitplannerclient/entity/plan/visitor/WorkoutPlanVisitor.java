package com.example.fitplannerclient.entity.plan.visitor;

import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.CompositeNode;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

public interface WorkoutPlanVisitor {
    void visit(WorkoutPlan workoutPlan);
    void visit(WorkoutSession workoutSession);

    void visit(ExerciseNode exerciseNode);

    void visit(CompositeNode compositeNode);
    void visit(FlowDecorator flowDecorator);
}
