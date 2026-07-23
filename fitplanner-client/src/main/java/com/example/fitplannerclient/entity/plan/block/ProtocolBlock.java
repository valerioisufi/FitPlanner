package com.example.fitplannerclient.entity.plan.block;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.block.strategy.composition.CompositionRule;
import com.example.fitplannerclient.entity.plan.block.strategy.validation.ValidationResult;
import com.example.fitplannerclient.entity.plan.block.strategy.validation.ValidationRule;
import com.example.fitplannerclient.entity.plan.decorator.ProgressionDecorator;
import com.example.fitplannerclient.entity.plan.execution.ExecutionContext;
import com.example.fitplannerclient.entity.plan.execution.ExecutionResult;
import com.example.fitplannerclient.entity.plan.execution.PlanNodeState;

import java.util.*;

public class ProtocolBlock extends CompositeNode {
    private final ProtocolType semanticType;

    private final List<ValidationRule> validationRules;
    private final List<CompositionRule> compositionRules;
    private final List<CompositionRule> blockCompositionRules;

    private final Block decoratedGroup; // gruppo di nodi decorati attraverso le composition rules (applyCompositionRules)
    private PlanNode internalExecutionRoot; // nodo radice per l'esecuzione

    private Map<String, String> parameters = new HashMap<>();

    public ProtocolBlock(ProtocolType semanticType, List<ValidationRule> validationRules, List<CompositionRule> compositionRules, List<CompositionRule> blockCompositionRules) {
        super(semanticType.toString());
        this.semanticType = semanticType;
        this.validationRules = validationRules;
        this.compositionRules = compositionRules;
        this.blockCompositionRules = blockCompositionRules;

        // gruppo di nodi decorati
        this.decoratedGroup = new Block(null);

        buildExecutionRoot();
    }

    @Override
    public CompositeNodeType getType() {
        return CompositeNodeType.PROTOCOL;
    }

    @Override
    public Optional<ProtocolType> getProtocolType() {
        return Optional.of(this.semanticType);
    }

    @Override
    public PlanNode deepCopy() {
        ProtocolBlock copy = new ProtocolBlock(this.semanticType, this.validationRules, this.compositionRules, this.blockCompositionRules);
        copy.parameters = new HashMap<>(this.parameters);
        for (int i = 0; i < this.getChildrenCount(); i++) {
            copy.addNode(this.getNodeAt(i).deepCopy());
        }
        return copy;
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
        super.addNode(node);
        decoratedGroup.addNode(applyCompositionRules(node));
    }

    @Override
    public void addNodeAt(int index, PlanNode node) {
        super.addNodeAt(index, node);
        decoratedGroup.addNodeAt(index, applyCompositionRules(node));
    }

    @Override
    public boolean removeNode(PlanNode node) {
        int idx = this.indexOf(node);
        if (idx != -1) {
            this.removeNodeAt(idx);
            return true;
        }
        return false;
    }

    @Override
    public PlanNode removeNodeAt(int index) {
        PlanNode removedNode = super.removeNodeAt(index);
        decoratedGroup.removeNodeAt(index);

        return removedNode;
    }

    @Override
    public PlanNode replaceNode(int index, PlanNode newNode) {
        PlanNode oldNode = super.replaceNode(index, newNode);
        decoratedGroup.replaceNode(index, applyCompositionRules(newNode));

        return oldNode;
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

        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                context.setParameter(entry.getKey(), entry.getValue());
            }
        }

        ExecutionResult result = internalExecutionRoot.execute(context);

        if (result.getState() == PlanNodeState.RUNNING || result.getState() == PlanNodeState.WAITING) {
            context.prependBreadcrumb(this.semanticType.toString());
            return result;
        }

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
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public void setParameter(String key, String value) {
        this.parameters.put(key, value);
    }

    @Override
    public Set<String> getExposedVariables() {
        Set<String> vars = new HashSet<>();

        for (Map.Entry<String, String> entry : this.getParameters().entrySet()) {
            vars.add(entry.getKey());

            if (entry.getValue() != null && entry.getValue().contains(":")) {
                vars.addAll(ProgressionDecorator.parseProgressions(entry.getValue()).keySet());
            }
        }

        return vars;
    }

    private PlanNode applyCompositionRules(PlanNode node) {
        if (compositionRules == null) return node;
        for (CompositionRule rule : compositionRules) {
            node = rule.apply(node);
        }
        return node;
    }

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();

        if (validationRules == null) return result;

        for (ValidationRule rule : validationRules) {
            ValidationResult ruleResult = rule.validate(this);
            result.getErrors().addAll(ruleResult.getErrors());
        }

        return result;
    }

}
