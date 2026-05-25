package com.example.fitplannerclient.entity.plan.block.strategy.composition;

import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseModifier;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

public class ModifierInjectionVisitor implements WorkoutPlanVisitor {

    private final ExerciseModifier modifier;

    public ModifierInjectionVisitor(ExerciseModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public void visit(WorkoutPlan workoutPlan) {}

    @Override
    public void visit(WorkoutSession workoutSession) {}

    @Override
    public void visit(ExerciseNode exerciseNode) {
        exerciseNode.addModifier(new ExerciseModifier(modifier));
    }

    @Override
    public void visit(Block block) {}

    @Override
    public void visit(ProtocolBlock protocolBlock) {}

    @Override
    public void visit(LoopDecorator loopDecorator) {}

    @Override
    public void visit(RestDecorator restDecorator) {}

    @Override
    public void visit(TimeLimitDecorator timeLimitDecorator) {}

    @Override
    public void visit(ProgressionDecorator progressionDecorator) {}

    @Override
    public void visit(IntervalDecorator intervalDecorator) {}
}
