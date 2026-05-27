package com.example.fitplannerclient.controller.plan.visitor;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

public class NodeFinderVisitor implements WorkoutPlanVisitor {
    private final String id;

    private PlanNode foundNode;

    private int foundPosition;
    private PlanNode foundParent;

    public NodeFinderVisitor(String id) {
        this.id = id;
    }

    public PlanNode getFoundNode() {
        return foundNode;
    }
    public int getFoundPosition() {
        return foundPosition;
    }
    public PlanNode getFoundParent() {
        return foundParent;
    }
    public boolean isFound() {
        return foundNode != null;
    }

    @Override
    public void visit(WorkoutPlan workoutPlan) {
        for (WorkoutSession session : workoutPlan.getSessions()) {
            session.accept(this);
        }
    }

    @Override
    public void visit(WorkoutSession workoutSession) {
        if (workoutSession.getRoot() != null) {
            workoutSession.getRoot().accept(this);
        }
    }

    @Override
    public void visit(ExerciseNode exerciseNode) {
        if(exerciseNode.getId().equals(id)) {
            foundNode = exerciseNode;
        }
    }

    @Override
    public void visit(Block block) {
        if(block.getId().equals(id)) {
            foundNode = block;
        } else {
            for (PlanNode child : block) {
                if (foundNode != null) break;

                foundParent = block;
                foundPosition = block.indexOf(child);
                child.accept(this);
            }
        }

    }

    @Override
    public void visit(ProtocolBlock protocolBlock) {
        if(protocolBlock.getId().equals(id)) {
            foundNode = protocolBlock;
        } else {
            for (PlanNode child : protocolBlock) {
                if (foundNode != null) break;

                foundParent = protocolBlock;
                foundPosition = protocolBlock.indexOf(child);
                child.accept(this);
            }
        }

    }

    private void visitDecorator(FlowDecorator decorator) {
        if(decorator.getId().equals(id)) {
            foundNode = decorator;
        } else {
            foundParent = decorator;
            foundPosition = -1;
            decorator.getWrappedNode().accept(this);
        }
    }

    @Override
    public void visit(LoopDecorator loopDecorator) {
        visitDecorator(loopDecorator);
    }

    @Override
    public void visit(RestDecorator restDecorator) {
        visitDecorator(restDecorator);
    }

    @Override
    public void visit(TimeLimitDecorator timeLimitDecorator) {
        visitDecorator(timeLimitDecorator);
    }

    @Override
    public void visit(ProgressionDecorator progressionDecorator) {
        visitDecorator(progressionDecorator);
    }

    @Override
    public void visit(IntervalDecorator intervalDecorator) {
        visitDecorator(intervalDecorator);
    }
}
