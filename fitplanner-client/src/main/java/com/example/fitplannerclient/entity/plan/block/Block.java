package com.example.fitplannerclient.entity.plan.block;

import com.example.fitplannerclient.entity.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.execution.ExecutionContext;
import com.example.fitplannerclient.entity.plan.execution.ExecutionResult;
import com.example.fitplannerclient.entity.plan.execution.PlanNodeState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Block extends PlanNode implements GroupNode {
    private String title;

    protected List<PlanNode> children = new ArrayList<>();
    private int currentChildIndex = 0;

    public Block(String title) {
        this.title = title;
    }

    @Override
    public PlanNode deepCopy() {
        Block copy = new Block(this.title);
        for (PlanNode child : this.children) {
            copy.addNode(child.deepCopy());
        }
        return copy;
    }

    @Override
    public void addNode(PlanNode node) {
        children.add(node);
    }

    @Override
    public void addNodeAt(int index, PlanNode node) {
        children.add(index, node);
    }

    @Override
    public boolean removeNode(PlanNode node) {
        return children.remove(node);
    }

    @Override
    public PlanNode removeNodeAt(int index) {
        return children.remove(index);
    }

    @Override
    public int getChildrenCount() {
        return children.size();
    }

    @Override
    public PlanNode getNodeAt(int index) {
        return children.get(index);
    }

    @Override
    public PlanNode replaceNode(int index, PlanNode newNode) {
        PlanNode oldNode = children.remove(index);
        children.add(index, newNode);

        return oldNode;
    }

    @Override
    public int indexOf(PlanNode node) {
        return children.indexOf(node);
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        if (this.state == PlanNodeState.COMPLETED) {
            return new ExecutionResult(PlanNodeState.COMPLETED);
        }

        if (this.state == PlanNodeState.IDLE) {
            this.state = PlanNodeState.RUNNING;
        }

        while (currentChildIndex >= 0 && currentChildIndex < children.size()) {
            ExecutionResult result = children.get(currentChildIndex).execute(context);

            if (result.getState() == PlanNodeState.COMPLETED || result.getState() == PlanNodeState.SKIPPED) {
                currentChildIndex++;
                // il ciclo while prosegue con il figlio successivo
            } else if (result.getState() == PlanNodeState.REVERT) {
                children.get(currentChildIndex).reset();

                if (currentChildIndex > 0) {
                    currentChildIndex--;
                    children.get(currentChildIndex).reset();

                } else {
                    return new ExecutionResult(PlanNodeState.REVERT);
                }

            } else {
                // il figlio è RUNNING o WAITING
                if (this.title != null && !this.title.isEmpty()) {
                    context.prependBreadcrumb(this.title);
                }
                return result;
            }
        }

        // se usciamo dal ciclo while, tutti i figli sono COMPLETED
        this.state = PlanNodeState.COMPLETED;
        return new ExecutionResult(PlanNodeState.COMPLETED);
    }

    @Override
    public void reset() {
        this.state = PlanNodeState.IDLE;
        currentChildIndex = 0;

        for (PlanNode child : children) {
            child.reset();
        }
    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Iterator<PlanNode> iterator() {
        return Collections.unmodifiableList(children).iterator();
    }
}
