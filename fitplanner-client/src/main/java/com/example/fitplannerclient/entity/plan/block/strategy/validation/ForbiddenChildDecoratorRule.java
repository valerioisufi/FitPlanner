package com.example.fitplannerclient.entity.plan.block.strategy.validation;

import com.example.fitplannerclient.entity.plan.visitor.EmptyWorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;

public class ForbiddenChildDecoratorRule implements ValidationRule {

    private final FlowDecoratorType forbiddenType;

    public ForbiddenChildDecoratorRule(FlowDecoratorType forbiddenType) {
        this.forbiddenType = forbiddenType;
    }

    @Override
    public ValidationResult validate(ProtocolBlock block) {
        ValidationResult result = new ValidationResult();

        WorkoutPlanVisitor checker = new EmptyWorkoutPlanVisitor() {

            @Override
            public void visit(FlowDecorator flowDecorator) { checkDecorator(flowDecorator.getType(), flowDecorator); }

            private void checkDecorator(FlowDecoratorType visitedType, FlowDecorator decorator) {
                if (visitedType == forbiddenType) {
                    result.addError(
                            "Questo blocco non ammette il decoratore interno: " + forbiddenType,
                            decorator.getId()
                    );
                } else {
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
