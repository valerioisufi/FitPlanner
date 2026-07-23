package com.example.fitplannerclient.entity.plan.block.strategy.validation;

import com.example.fitplannerclient.entity.plan.block.CompositeNode;
import com.example.fitplannerclient.entity.plan.visitor.EmptyWorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

public class OnlyExercisesAllowedRule implements ValidationRule {

    @Override
    public ValidationResult validate(ProtocolBlock block) {
        ValidationResult result = new ValidationResult();

        WorkoutPlanVisitor checker = new EmptyWorkoutPlanVisitor() {

            // nodo non valido
            @Override public void visit(CompositeNode compositeNode) { addError(compositeNode); }

            @Override public void visit(FlowDecorator flowDecorator) { flowDecorator.getWrappedNode().accept(this); }

            private void addError(PlanNode node) {
                result.addError(
                        "Tipo di nodo non consentito in questo blocco. Sono ammessi solo esercizi singoli.",
                        node.getId()
                );
            }
        };

        for (PlanNode child : block) {
            child.accept(checker);
        }

        return result;
    }
}
