package com.example.fitplannerclient.entity.plan.block.strategy.validation;

import com.example.fitplannerclient.entity.plan.visitor.EmptyWorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.visitor.WorkoutPlanVisitor;
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

            @Override public void visit(LoopDecorator loopDecorator) { visitWrapped(loopDecorator); }
            @Override public void visit(RestDecorator restDecorator) { visitWrapped(restDecorator); }
            @Override public void visit(TimeLimitDecorator timeLimitDecorator) { visitWrapped(timeLimitDecorator); }
            @Override public void visit(ProgressionDecorator progressionDecorator) { visitWrapped(progressionDecorator); }
            @Override public void visit(IntervalDecorator intervalDecorator) { visitWrapped(intervalDecorator); }

            private void visitWrapped(FlowDecorator decorator) {
                if (decorator.getWrappedNode() != null) {
                    decorator.getWrappedNode().accept(this);
                }
            }
        };

        for (PlanNode child : block) {
            child.accept(checker);
        }

        return result;
    }
}
