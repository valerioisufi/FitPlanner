package com.example.fitplannerclient.entity.plan.block.strategy.composition;

import com.example.fitplannerclient.entity.plan.visitor.EmptyWorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseModifier;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

public class DefaultModifierRule implements CompositionRule {
    private final ExerciseModifier modifier;

    public DefaultModifierRule(ExerciseModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public PlanNode apply(PlanNode node) {

        WorkoutPlanVisitor visitor = new EmptyWorkoutPlanVisitor() {

            @Override
            public void visit(ExerciseNode exerciseNode) {
                exerciseNode.addModifier(new ExerciseModifier(modifier));
            }

            @Override
            public void visit(Block block) {
                for (PlanNode child : block) {
                    child.accept(this);
                }
            }

            @Override
            public void visit(ProtocolBlock protocolBlock) {
                for (PlanNode child : protocolBlock) {
                    child.accept(this);
                }
            }

            @Override public void visit(FlowDecorator flowDecorator) {
                flowDecorator.getWrappedNode().accept(this);
            }

        };

        node.accept(visitor);
        return node;
    }
}
