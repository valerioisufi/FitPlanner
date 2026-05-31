package com.example.fitplannerclient.controller.plan.core.visitor;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.GroupNode;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

public abstract class GroupingWorkoutPlanVisitor implements WorkoutPlanVisitor {
    public void visitGroupNode(GroupNode groupNode) {}

    public void visitFlowDecorator(FlowDecorator flowDecorator) {}

    public void visitLeafNode(PlanNode leafNode) {}

    @Override
    public void visit(WorkoutPlan workoutPlan) {}

    @Override
    public void visit(WorkoutSession workoutSession) {}

    @Override
    public void visit(ExerciseNode exerciseNode) {
        visitLeafNode(exerciseNode);
    }

    @Override
    public void visit(Block block) {
        visitGroupNode(block);
    }

    @Override
    public void visit(ProtocolBlock protocolBlock) {
        visitGroupNode(protocolBlock);
    }

    @Override
    public void visit(LoopDecorator loopDecorator) {
        visitFlowDecorator(loopDecorator);
    }

    @Override
    public void visit(RestDecorator restDecorator) {
        visitFlowDecorator(restDecorator);
    }

    @Override
    public void visit(TimeLimitDecorator timeLimitDecorator) {
        visitFlowDecorator(timeLimitDecorator);
    }

    @Override
    public void visit(ProgressionDecorator progressionDecorator) {
        visitFlowDecorator(progressionDecorator);
    }

    @Override
    public void visit(IntervalDecorator intervalDecorator) {
        visitFlowDecorator(intervalDecorator);
    }
}
