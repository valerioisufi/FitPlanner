package com.example.fitplannerclient.entity.plan.block.strategy.validation;

import com.example.fitplannerclient.controller.plan.core.visitor.EmptyWorkoutPlanVisitor;
import com.example.fitplannerclient.controller.plan.core.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import com.example.fitplannerclient.entity.plan.exercise.ModifierType;

public class ForbiddenModifierRule implements ValidationRule {
    private final ModifierType forbiddenType;

    public ForbiddenModifierRule(ModifierType forbiddenType) {
        this.forbiddenType = forbiddenType;
    }

    @Override
    public ValidationResult validate(ProtocolBlock block) {
        ValidationResult result = new ValidationResult();

        WorkoutPlanVisitor checker = new EmptyWorkoutPlanVisitor() {

            @Override
            public void visit(ExerciseNode exerciseNode) {
                if (exerciseNode.hasModifier(forbiddenType)) {
                    result.addError(
                            "L'esercizio contiene un modificatore vietato per questo blocco: " + forbiddenType,
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
