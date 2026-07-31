package com.example.fitplannerclient.entity.plan.block;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.execution.ExecutionContext;
import com.example.fitplannerclient.entity.plan.execution.ExecutionResult;
import com.example.fitplannerclient.entity.plan.execution.PlanNodeState;

public class Block extends CompositeNode {
    private int currentChildIndex = 0;

    public Block(String title) {
        super(title);
    }

    @Override
    public CompositeNodeType getType() {
        return CompositeNodeType.BLOCK;
    }

    @Override
    public PlanNode deepCopy() {
        Block copy = new Block(this.getName().orElse(null));
        for (PlanNode child : this) {
            copy.addNode(child.deepCopy());
        }
        return copy;
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        if (this.state == PlanNodeState.COMPLETED) {
            return new ExecutionResult(PlanNodeState.COMPLETED);
        }

        if (this.state == PlanNodeState.IDLE) {
            this.state = PlanNodeState.RUNNING;
        }

        while (currentChildIndex >= 0 && currentChildIndex < this.getChildrenCount()) {
            ExecutionResult result = executeChild(context);
            if (result != null) {
                return result;
            }
        }

        // se usciamo dal ciclo while, tutti i figli sono COMPLETED
        this.state = PlanNodeState.COMPLETED;
        return new ExecutionResult(PlanNodeState.COMPLETED);
    }

    private ExecutionResult executeChild(ExecutionContext context) {
        ExecutionResult result = this.getNodeAt(currentChildIndex).execute(context);

        if (result.getState() == PlanNodeState.COMPLETED || result.getState() == PlanNodeState.SKIPPED) {
            currentChildIndex++;
            // il ciclo while prosegue con il figlio successivo
        } else if (result.getState() == PlanNodeState.REVERT) {
            this.getNodeAt(currentChildIndex).reset();

            if (currentChildIndex > 0) {
                currentChildIndex--;
                this.getNodeAt(currentChildIndex).reset();

            } else {
                return new ExecutionResult(PlanNodeState.REVERT);
            }

        } else {
            // il figlio è RUNNING o WAITING
            this.getName().ifPresent(name -> {
                if (!name.isEmpty()) {
                    context.prependBreadcrumb(name);
                }
            });

            return result;
        }

        return null;
    }

    @Override
    public void reset() {
        this.state = PlanNodeState.IDLE;
        currentChildIndex = 0;

        for (PlanNode child : this) {
            child.reset();
        }
    }

}
