package com.example.fitplannerclient.entity.plan.block;

import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.block.strategy.composition.CompositionRule;
import com.example.fitplannerclient.entity.plan.block.strategy.validation.ValidationResult;
import com.example.fitplannerclient.entity.plan.block.strategy.validation.ValidationRule;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;

import java.util.Iterator;
import java.util.List;

public class ProtocolBlock extends PlanNode implements GroupNode {
    private String semanticType;

    private List<ValidationRule> validationRules;
    private List<CompositionRule> compositionRules;
    private List<CompositionRule> blockCompositionRules;

    private final Block rawGroup; // gruppo di nodi non decorati
    private final Block decoratedGroup; // gruppo di nodi decorati attraverso le composition rules (applyCompositionRules)
    private PlanNode internalExecutionRoot; // nodo radice per l'esecuzione

    public ProtocolBlock(String semanticType, List<ValidationRule> validationRules, List<CompositionRule> compositionRules, List<CompositionRule> blockCompositionRules) {
        this.semanticType = semanticType;
        this.validationRules = validationRules;
        this.compositionRules = compositionRules;
        this.blockCompositionRules = blockCompositionRules;

        this.rawGroup = new Block(semanticType + " (Raw)");
        this.decoratedGroup = new Block(semanticType + " (Decorated)");

        buildExecutionRoot();
    }

    private void buildExecutionRoot() {
        PlanNode root = decoratedGroup;

        if (blockCompositionRules != null) {
            for (CompositionRule rule : blockCompositionRules) {
                root = rule.apply(root);
            }
        }

        this.internalExecutionRoot = root;
    }

    @Override
    public void addNode(PlanNode node) {
        rawGroup.addNode(node);
        decoratedGroup.addNode(applyCompositionRules(node));
    }

    @Override
    public void addNodeAt(int index, PlanNode node) {
        rawGroup.addNodeAt(index, node);
        decoratedGroup.addNodeAt(index, applyCompositionRules(node));
    }

    @Override
    public boolean removeNode(PlanNode node) {
        int idx = rawGroup.indexOf(node);
        if (idx != -1) {
            rawGroup.removeNodeAt(idx);
            decoratedGroup.removeNodeAt(idx);
            return true;
        }
        return false;
    }

    @Override
    public PlanNode removeNodeAt(int index) {
        PlanNode removedNode = rawGroup.removeNodeAt(index);
        decoratedGroup.removeNodeAt(index);

        return removedNode;
    }

    @Override
    public PlanNode replaceNode(int index, PlanNode newNode) {
        PlanNode oldNode = rawGroup.replaceNode(index, newNode);
        decoratedGroup.replaceNode(index, applyCompositionRules(newNode));

        return oldNode;
    }

    @Override
    public int getChildrenCount() {
        return rawGroup.getChildrenCount();
    }

    @Override
    public PlanNode getNodeAt(int index) {
        return rawGroup.getNodeAt(index);
    }

    @Override
    public int indexOf(PlanNode node) {
        return rawGroup.indexOf(node);
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        if (this.state == PlanNodeState.COMPLETED) {
            return new ExecutionResult(PlanNodeState.COMPLETED);
        }

        if (this.state == PlanNodeState.IDLE) {
            this.state = PlanNodeState.RUNNING;
            internalExecutionRoot.reset();
        }

        ExecutionResult result = internalExecutionRoot.execute(context);

        if (result.getState() == PlanNodeState.COMPLETED) {
            this.state = PlanNodeState.COMPLETED;
        }
        
        return result;
    }

    @Override
    public void reset() {
        this.state = PlanNodeState.IDLE;
        internalExecutionRoot.reset();
    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    public String getSemanticType() {
        return semanticType;
    }

    public void setSemanticType(String semanticType) {
        this.semanticType = semanticType;
    }

    private PlanNode applyCompositionRules(PlanNode node) {
        if (compositionRules == null) return node;
        for (CompositionRule rule : compositionRules) {
            node = rule.apply(node);
        }
        return node;
    }

    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();

        if (validationRules == null) return result;

        for (ValidationRule rule : validationRules) {
            ValidationResult ruleResult = rule.validate(this);
            result.getErrors().addAll(ruleResult.getErrors());
        }

        return result;
    }

    @Override
    public Iterator<PlanNode> iterator() {
        return rawGroup.iterator();
    }
}
