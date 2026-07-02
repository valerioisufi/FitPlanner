package com.example.fitplannerclient.entity.plan.visitor;

import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

public interface WorkoutPlanVisitor {
    void visit(WorkoutPlan workoutPlan);
    void visit(WorkoutSession workoutSession);

    void visit(ExerciseNode exerciseNode);

    void visit(Block block);
    void visit(ProtocolBlock protocolBlock);

    void visit(LoopDecorator loopDecorator);
    void visit(RestDecorator restDecorator);
    void visit(TimeLimitDecorator timeLimitDecorator);
    void visit(ProgressionDecorator progressionDecorator);
    void visit(IntervalDecorator intervalDecorator);
}
