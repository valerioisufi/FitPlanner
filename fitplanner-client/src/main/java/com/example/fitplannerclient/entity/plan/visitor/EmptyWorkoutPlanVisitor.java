package com.example.fitplannerclient.entity.plan.visitor;

import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.CompositeNode;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

/**
 * Un'implementazione base del WorkoutPlanVisitor che fornisce implementazioni vuote
 * per tutti i metodi, utile quando si vuole creare un visitor che gestisce solo
 * specifici tipi di nodi
 */
public abstract class EmptyWorkoutPlanVisitor implements WorkoutPlanVisitor {
    
    @Override public void visit(WorkoutPlan workoutPlan) {}
    @Override public void visit(WorkoutSession workoutSession) {}

    @Override public void visit(ExerciseNode exerciseNode) {}

    @Override public void visit(CompositeNode compositeNode) {}
    @Override public void visit(FlowDecorator flowDecorator) {}

}
