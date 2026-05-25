package com.example.fitplannerclient.entity.plan.block.strategy.composition;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseModifier;

public class DefaultModifierRule implements CompositionRule {
    private final ExerciseModifier modifier;

    public DefaultModifierRule(ExerciseModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public PlanNode apply(PlanNode node) {
        ModifierInjectionVisitor visitor = new ModifierInjectionVisitor(modifier);

        node.accept(visitor);
        return node;
    }
}
