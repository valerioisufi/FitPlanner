package com.example.fitplannerclient.entity.plan.block;

import com.example.fitplannerclient.controller.plan.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.block.strategy.CompositionRule;
import com.example.fitplannerclient.entity.plan.block.strategy.ValidationRule;

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
    public void execute() {

    }

    @Override
    public void reset() {

    }
}
