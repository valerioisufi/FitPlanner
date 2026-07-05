package com.example.fitplannerclient.entity.plan.visitor;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.GroupNode;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

import java.util.ArrayList;
import java.util.List;

public class NodeFinderVisitor implements WorkoutPlanVisitor {
    protected final String id;

    private PlanNode foundNode;

    private PlanNode foundParent;
    private int foundPosition;

    private GroupNode foundGroupNodeParent;
    private int foundGroupNodePosition;
    private int foundGroupNodeIndex; // indice nel foundPath

    private List<PlanNode> foundPath = new ArrayList<>();

    private final List<PlanNode> currentPath = new ArrayList<>();

    public NodeFinderVisitor(String id) {
        this.id = id;
    }

    public PlanNode getFoundNode() {
        return foundNode;
    }
    public PlanNode getFoundParent() {
        return foundParent;
    }
    public int getFoundPosition() {
        return foundPosition;
    }

    public GroupNode getFoundGroupNodeParent() {
        return foundGroupNodeParent;
    }
    public int getFoundGroupNodePosition() {
        return foundGroupNodePosition;
    }
    public int getFoundGroupNodeIndex() {
        return foundGroupNodeIndex;
    }

    public PlanNode getFoundOutmostNode() {
        return foundGroupNodeParent.getNodeAt(foundGroupNodePosition);
    }

    public List<PlanNode> getFoundPath() {
        return foundPath;
    }
    public boolean isFound() {
        return foundNode != null;
    }

    @Override
    public void visit(WorkoutPlan workoutPlan) {
        for (WorkoutSession session : workoutPlan.getSessions()) {
            if (foundNode != null) break;
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
        currentPath.addLast(exerciseNode);

        if(exerciseNode.getId().equals(id)) {
            foundNode = exerciseNode;
            foundPath = new ArrayList<>(currentPath);
        }

        currentPath.removeLast();
    }

    private <T extends PlanNode & GroupNode> void visitGroupNode(T groupNode) {
        currentPath.addLast(groupNode);

        if(groupNode.getId().equals(id)) {
            foundNode = groupNode;
            foundPath = new ArrayList<>(currentPath);
        } else {
            for (PlanNode child : groupNode) {
                if (foundNode != null) break;

                foundParent = groupNode;
                foundPosition = groupNode.indexOf(child);

                foundGroupNodeParent = groupNode;
                foundGroupNodePosition = groupNode.indexOf(child);
                foundGroupNodeIndex = currentPath.size() - 1;
                
                child.accept(this);
                
                if (foundNode == null) {
                    foundParent = null;
                    foundPosition = -1;
                    foundGroupNodeParent = null;
                    foundGroupNodePosition = -1;
                    foundGroupNodeIndex = -1;
                }
            }
        }
        currentPath.removeLast();
    }

    @Override
    public void visit(Block block) {
        visitGroupNode(block);
    }

    @Override
    public void visit(ProtocolBlock protocolBlock) {
        visitGroupNode(protocolBlock);
    }

    private void visitDecorator(FlowDecorator decorator) {
        currentPath.addLast(decorator);

        if(decorator.getId().equals(id)) {
            foundNode = decorator;
            foundPath = new ArrayList<>(currentPath);
        } else {
            foundParent = decorator;
            foundPosition = -1;

            if (decorator.getWrappedNode() != null)
                decorator.getWrappedNode().accept(this);
                
            if (foundNode == null) {
                foundParent = null;
                foundPosition = -1;
            }
        }
        currentPath.removeLast();
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
