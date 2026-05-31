package com.example.fitplannerclient.entity.plan.block.strategy.validation;

import com.example.fitplannerclient.controller.plan.core.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import com.example.fitplannerclient.entity.plan.exercise.ModifierType;

public class RequiredModifierRule implements ValidationRule {
    private final ModifierType requiredType;

    public RequiredModifierRule(ModifierType requiredType) {
        this.requiredType = requiredType;
    }

    @Override
    public ValidationResult validate(ProtocolBlock block) {
        ValidationResult result = new ValidationResult();

        WorkoutPlanVisitor checker = new WorkoutPlanVisitor() {
            @Override public void visit(WorkoutPlan workoutPlan) {}
            @Override public void visit(WorkoutSession workoutSession) {}
            @Override public void visit(Block blockNode) {}
            @Override public void visit(ProtocolBlock protocolBlock) {}

            @Override
            public void visit(ExerciseNode exerciseNode) {
                if (!exerciseNode.hasModifier(requiredType)) {
                    result.addError(
                            "L'esercizio manca di un modificatore obbligatorio per questo blocco: " + requiredType,
                            exerciseNode.getId()
                    );
                }
            }

            @Override public void visit(LoopDecorator loopDecorator) { loopDecorator.getWrappedNode().accept(this); }
            @Override public void visit(RestDecorator restDecorator) { restDecorator.getWrappedNode().accept(this); }
            @Override public void visit(TimeLimitDecorator timeLimitDecorator) { timeLimitDecorator.getWrappedNode().accept(this); }
            @Override public void visit(ProgressionDecorator progressionDecorator) { progressionDecorator.getWrappedNode().accept(this); }
            @Override public void visit(IntervalDecorator intervalDecorator) { intervalDecorator.getWrappedNode().accept(this); }
        };

        for (PlanNode child : block) {
            child.accept(checker);
        }

        return result;
    }
}
