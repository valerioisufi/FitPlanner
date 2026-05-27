package com.example.fitplannerclient.controller.plan.visitor;

import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

public abstract class EmpyWorkoutPlanVisitor implements WorkoutPlanVisitor {
    public void visit(WorkoutPlan workoutPlan) {}
    public void visit(WorkoutSession workoutSession) {}

    public void visit(ExerciseNode exerciseNode) {}

    public void visit(Block block) {}
    public void visit(ProtocolBlock protocolBlock) {}

    public void visit(LoopDecorator loopDecorator) {}
    public void visit(RestDecorator restDecorator) {}
    public void visit(TimeLimitDecorator timeLimitDecorator) {}
    public void visit(ProgressionDecorator progressionDecorator) {}
    public void visit(IntervalDecorator intervalDecorator) {}
}
