package com.example.fitplannerclient.entity.plan.visitor;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.block.strategy.validation.ValidationResult;
import com.example.fitplannerclient.entity.plan.decorator.FlowDecorator;
import com.example.fitplannerclient.entity.plan.decorator.IntervalDecorator;
import com.example.fitplannerclient.entity.plan.decorator.LoopDecorator;
import com.example.fitplannerclient.entity.plan.decorator.ProgressionDecorator;
import com.example.fitplannerclient.entity.plan.decorator.RestDecorator;
import com.example.fitplannerclient.entity.plan.decorator.TimeLimitDecorator;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

/**
 * Attraversa l'intero piano e aggrega gli errori di validazione di tutti i ProtocolBlocks
 */
public class ProtocolValidationVisitor implements WorkoutPlanVisitor {

    private final ValidationResult result = new ValidationResult();

    public ValidationResult getResult() {
        return result;
    }

    @Override
    public void visit(WorkoutPlan workoutPlan) {
        for (WorkoutSession session : workoutPlan.getSessions()) {
            session.accept(this);
        }
    }

    @Override
    public void visit(WorkoutSession workoutSession) {
        if (workoutSession.getRoot() != null) {
            workoutSession.getRoot().accept(this);
        }
    }

    @Override
    public void visit(ExerciseNode exerciseNode) {
        // metodo intenzionalmente vuoto
    }

    @Override
    public void visit(Block block) {
        for (PlanNode child : block) {
            child.accept(this);
        }
    }

    @Override
    public void visit(ProtocolBlock protocolBlock) {
        result.addErrors(protocolBlock.validate().getErrors());

        for (PlanNode child : protocolBlock) {
            child.accept(this);
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
}
