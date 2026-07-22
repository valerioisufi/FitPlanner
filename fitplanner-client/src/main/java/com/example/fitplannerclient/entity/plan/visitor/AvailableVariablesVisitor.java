package com.example.fitplannerclient.entity.plan.visitor;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
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

            if (n instanceof ProtocolBlock pb && pb.getParameters() != null) {
                for (Map.Entry<String, String> entry : pb.getParameters().entrySet()) {
                    vars.add(entry.getKey());

                    if (entry.getValue() != null && entry.getValue().contains(":")) {
                        vars.addAll(ProgressionDecorator.parseProgressions(entry.getValue()).keySet());
                    }
                }

            } else if (n instanceof ProgressionDecorator pd && pd.getSerializedValue() != null) {
                    vars.addAll(ProgressionDecorator.parseProgressions(pd.getSerializedValue()).keySet());
                }

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
    public void visit(Block block) {
        enterNode(block);
        checkFound(block);
        if (!found) {
            for (int i = 0; i < block.getChildrenCount(); i++) {
                if (!found) block.getNodeAt(i).accept(this);
            }
        }
        exitNode();
    }

    @Override
    public void visit(ProtocolBlock protocolBlock) {
        enterNode(protocolBlock);
        checkFound(protocolBlock);
        if (!found) {
            for (int i = 0; i < protocolBlock.getChildrenCount(); i++) {
                if (!found) protocolBlock.getNodeAt(i).accept(this);
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
