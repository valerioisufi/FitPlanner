package com.example.fitplannerclient.entity.plan.visitor;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.CompositeNode;
import com.example.fitplannerclient.entity.plan.block.GroupNode;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NodeFinderVisitor implements WorkoutPlanVisitor {
    protected final String id;

    private PlanNode foundNode;
    private ExerciseNode foundExerciseNode;
    private FlowDecorator foundFlowDecorator;
    private CompositeNode foundCompositeNode;

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

    public Optional<PlanNode> getFoundNode() {
        return Optional.ofNullable(foundNode);
    }
    public Optional<ExerciseNode> getFoundExerciseNode() {
        return Optional.ofNullable(foundExerciseNode);
    }
    public Optional<FlowDecorator> getFoundFlowDecorator() {
        return Optional.ofNullable(foundFlowDecorator);
    }
    public Optional<CompositeNode> getFoundCompositeNode() {
        return Optional.ofNullable(foundCompositeNode);
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
            foundExerciseNode = exerciseNode;
            foundPath = new ArrayList<>(currentPath);
        }

        currentPath.removeLast();
    }

    @Override
    public void visit(CompositeNode compositeNode) {
        currentPath.addLast(compositeNode);

        if(compositeNode.getId().equals(id)) {
            foundNode = compositeNode;
            foundCompositeNode = compositeNode;
            foundPath = new ArrayList<>(currentPath);
        } else {
            for (PlanNode child : compositeNode) {
                if (foundNode != null) break;

                foundParent = compositeNode;
                foundPosition = compositeNode.indexOf(child);

                foundGroupNodeParent = compositeNode;
                foundGroupNodePosition = compositeNode.indexOf(child);
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
    public void visit(FlowDecorator decorator) {
        currentPath.addLast(decorator);

        if(decorator.getId().equals(id)) {
            foundNode = decorator;
            foundFlowDecorator = decorator;
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

}
