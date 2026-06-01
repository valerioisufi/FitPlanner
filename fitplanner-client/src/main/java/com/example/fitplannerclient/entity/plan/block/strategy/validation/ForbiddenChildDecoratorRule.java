package com.example.fitplannerclient.entity.plan.block.strategy.validation;

import com.example.fitplannerclient.controller.plan.core.visitor.EmptyWorkoutPlanVisitor;
import com.example.fitplannerclient.controller.plan.core.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;

public class ForbiddenChildDecoratorRule implements ValidationRule {

    public enum FlowDecoratorType {
        LOOP, REST, TIME_LIMIT, INTERVAL, PROGRESSION
    }

    private final FlowDecoratorType forbiddenType;

    public ForbiddenChildDecoratorRule(FlowDecoratorType forbiddenType) {
        this.forbiddenType = forbiddenType;
    }

    @Override
    public ValidationResult validate(ProtocolBlock block) {
        ValidationResult result = new ValidationResult();

        WorkoutPlanVisitor checker = new EmptyWorkoutPlanVisitor() {

            @Override
            public void visit(LoopDecorator loopDecorator) { checkDecorator(FlowDecoratorType.LOOP, loopDecorator); }
            @Override
            public void visit(RestDecorator restDecorator) { checkDecorator(FlowDecoratorType.REST, restDecorator); }
            @Override
            public void visit(TimeLimitDecorator timeLimitDecorator) { checkDecorator(FlowDecoratorType.TIME_LIMIT, timeLimitDecorator); }
            @Override
            public void visit(ProgressionDecorator progressionDecorator) { checkDecorator(FlowDecoratorType.PROGRESSION, progressionDecorator); }
            @Override
            public void visit(IntervalDecorator intervalDecorator) { checkDecorator(FlowDecoratorType.INTERVAL, intervalDecorator); }

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
