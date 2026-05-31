package com.example.fitplannerclient.controller.plan.visitor;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

import java.util.ArrayList;
import java.util.List;

public class NodePathVisitor implements WorkoutPlanVisitor {
    private final String targetId;
    private final List<PlanNode> currentPath = new ArrayList<>();
    private List<PlanNode> foundPath = null;

    public NodePathVisitor(String targetId) {
        this.targetId = targetId;
    }

    public List<PlanNode> getPath() {
        return foundPath;
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
        if (foundPath != null) return;
        currentPath.add(exerciseNode);
        if (exerciseNode.getId().equals(targetId)) {
            foundPath = new ArrayList<>(currentPath);
        }
        currentPath.remove(currentPath.size() - 1);
    }

    @Override
    public void visit(Block block) {
        if (foundPath != null) return;
        currentPath.add(block);
        if (block.getId().equals(targetId)) {
            foundPath = new ArrayList<>(currentPath);
        } else {
            for (int i = 0; i < block.getChildrenCount(); i++) {
                block.getNodeAt(i).accept(this);
            }
        }
        currentPath.remove(currentPath.size() - 1);
    }

    @Override
    public void visit(ProtocolBlock protocolBlock) {
        if (foundPath != null) return;
        currentPath.add(protocolBlock);
        if (protocolBlock.getId().equals(targetId)) {
            foundPath = new ArrayList<>(currentPath);
        } else {
            for (int i = 0; i < protocolBlock.getChildrenCount(); i++) {
                protocolBlock.getNodeAt(i).accept(this);
            }
        }
        currentPath.remove(currentPath.size() - 1);
    }

    @Override
    public void visit(LoopDecorator loopDecorator) {
        handleDecorator(loopDecorator);
    }

    @Override
    public void visit(RestDecorator restDecorator) {
        handleDecorator(restDecorator);
    }

    @Override
    public void visit(TimeLimitDecorator timeLimitDecorator) {
        handleDecorator(timeLimitDecorator);
    }

    @Override
    public void visit(IntervalDecorator intervalDecorator) {
        handleDecorator(intervalDecorator);
    }

    @Override
    public void visit(ProgressionDecorator progressionDecorator) {
        handleDecorator(progressionDecorator);
    }

    private void handleDecorator(FlowDecorator decorator) {
        if (foundPath != null) return;
        currentPath.add(decorator);
        if (decorator.getId().equals(targetId)) {
            foundPath = new ArrayList<>(currentPath);
        } else {
            decorator.getWrappedNode().accept(this);
        }
        currentPath.remove(currentPath.size() - 1);
    }
}
