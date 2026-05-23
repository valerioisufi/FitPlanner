package com.example.fitplannerclient.entity.plan.block;

import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.block.strategy.composition.CompositionRule;
import com.example.fitplannerclient.entity.plan.block.strategy.validation.ValidationRule;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;

import java.util.List;

public class ProtocolBlock extends GroupNode{
    private String semanticType;
    private PlanNode internalExecutionRoot;

    private List<ValidationRule> validationRules;
    private List<CompositionRule> compositionRules;

    @Override
    public void addNodeAt(int index, PlanNode node) {

    }

    @Override
    public void replaceNode() {

    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        // TODO
        return null;
    }

    @Override
    public void reset() {

    }

    public String getSemanticType() {
        return semanticType;
    }

    public void setSemanticType(String semanticType) {
        this.semanticType = semanticType;
    }

    public PlanNode getInternalExecutionRoot() {
        return internalExecutionRoot;
    }

    public void setInternalExecutionRoot(PlanNode internalExecutionRoot) {
        this.internalExecutionRoot = internalExecutionRoot;
    }
}
