package com.example.fitplannerclient.entity.plan.visitor;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.CompositeNode;
import com.example.fitplannerclient.entity.plan.block.strategy.validation.ValidationResult;
import com.example.fitplannerclient.entity.plan.decorator.FlowDecorator;
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
        result.addErrors(exerciseNode.validate().getErrors());
    }

    @Override
    public void visit(CompositeNode compositeNode) {
        result.addErrors(compositeNode.validate().getErrors());

        for (PlanNode child : compositeNode) {
            child.accept(this);
        }
    }

    @Override
    public void visit(FlowDecorator flowDecorator) {
        result.addErrors(flowDecorator.validate().getErrors());

        if(flowDecorator.getWrappedNode() != null) {
            flowDecorator.getWrappedNode().accept(this);
        }
    }

}
