package com.example.fitplannerclient.entity.plan.block.strategy.composition;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.decorator.FlowDecorator;

public class WrapWithDecoratorRule implements CompositionRule {
    private final FlowDecorator decorator;

    public WrapWithDecoratorRule(FlowDecorator decorator) {
        this.decorator = decorator;
    }

    @Override
    public PlanNode apply(PlanNode node) {
        return decorator.cloneWithNode(node);
    }
}
