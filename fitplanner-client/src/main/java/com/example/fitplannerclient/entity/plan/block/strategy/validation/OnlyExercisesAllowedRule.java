package com.example.fitplannerclient.entity.plan.block.strategy.validation;

import com.example.fitplannerclient.controller.plan.core.visitor.EmptyWorkoutPlanVisitor;
import com.example.fitplannerclient.controller.plan.core.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

public class OnlyExercisesAllowedRule implements ValidationRule {

    @Override
    public ValidationResult validate(ProtocolBlock block) {
        ValidationResult result = new ValidationResult();

        WorkoutPlanVisitor checker = new EmptyWorkoutPlanVisitor() {
            // nodi validi
            @Override public void visit(ExerciseNode exerciseNode) {
                // nessun errore
            }

            // nodi non validi
            @Override public void visit(Block blockNode) { addError(blockNode); }
            @Override public void visit(ProtocolBlock protocolBlock) { addError(protocolBlock); }
            @Override public void visit(LoopDecorator loopDecorator) { addError(loopDecorator); }
            @Override public void visit(RestDecorator restDecorator) { addError(restDecorator); }
            @Override public void visit(TimeLimitDecorator timeLimitDecorator) { addError(timeLimitDecorator); }
            @Override public void visit(ProgressionDecorator progressionDecorator) { addError(progressionDecorator); }
            @Override public void visit(IntervalDecorator intervalDecorator) { addError(intervalDecorator); }



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
