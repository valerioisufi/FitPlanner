package com.example.fitplannerclient.entity.plan.visitor;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.CompositeNode;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

import java.util.*;

public class AvailableVariablesVisitor implements WorkoutPlanVisitor {
    private final String targetNodeId;
    private boolean found = false;

    private final LinkedList<PlanNode> currentPath = new LinkedList<>();
    private final List<String> availableVariables = new ArrayList<>();

    public AvailableVariablesVisitor(String targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public List<String> getAvailableVariables() {
        return availableVariables;
    }

    private void enterNode(PlanNode node) {
        if (!found) currentPath.addLast(node);
    }

    private void exitNode() {
        if (!found && !currentPath.isEmpty()) currentPath.removeLast();
    }

    private void checkFound(PlanNode node) {
        if (node.getId().equals(targetNodeId)) {
            found = true;
            extractVariablesFromPath();
        }
    }

    private void extractVariablesFromPath() {
        Set<String> vars = new HashSet<>();
        // Iterate up to size() - 1 to exclude the target node itself from providing variables
        for (int i = 0; i < currentPath.size() - 1; i++) {
            PlanNode n = currentPath.get(i);

            vars.addAll(n.getExposedVariables());
        }

        for (String varName : vars) {
            availableVariables.add("${" + varName + "}");
        }
    }

    @Override
    public void visit(WorkoutPlan workoutPlan) {
        for (WorkoutSession session : workoutPlan.getSessions()) {
            if (!found) session.accept(this);
        }
    }

    @Override
    public void visit(WorkoutSession workoutSession) {
        if (workoutSession.getRoot() != null && !found) {
            workoutSession.getRoot().accept(this);
        }
    }

    @Override
    public void visit(ExerciseNode exerciseNode) {
        enterNode(exerciseNode);
        checkFound(exerciseNode);
        exitNode();
    }

    @Override
    public void visit(CompositeNode compositeNode) {
        enterNode(compositeNode);
        checkFound(compositeNode);
        if (!found) {
            for (int i = 0; i < compositeNode.getChildrenCount(); i++) {
                if (!found) compositeNode.getNodeAt(i).accept(this);
            }
        }
        exitNode();
    }

    @Override
    public void visit(FlowDecorator flowDecorator) {
        enterNode(flowDecorator);
        checkFound(flowDecorator);
        if (!found && flowDecorator.getWrappedNode() != null) {
            flowDecorator.getWrappedNode().accept(this);
        }
        exitNode();
    }
}
