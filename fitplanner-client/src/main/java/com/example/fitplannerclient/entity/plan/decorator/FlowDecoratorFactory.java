package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.entity.plan.PlanNode;

public class FlowDecoratorFactory {

    public FlowDecorator createLoopDecorator(PlanNode wrappedNode, String roundsExpression) {
        return new LoopDecorator(wrappedNode, roundsExpression);
    }

    public FlowDecorator createRestDecorator(PlanNode wrappedNode, String restExpression) {
        return new RestDecorator(wrappedNode, restExpression);
    }

    public FlowDecorator createTimeLimitDecorator(PlanNode wrappedNode, String timeLimitExpression) {
        return new TimeLimitDecorator(wrappedNode, timeLimitExpression);
    }

    public FlowDecorator createIntervalDecorator(PlanNode wrappedNode, String intervalExpression) {
        return new IntervalDecorator(wrappedNode, intervalExpression);
    }

    public FlowDecorator createProgressionDecorator(PlanNode wrappedNode, String progressExpression) {
        return new ProgressionDecorator(wrappedNode, progressExpression);
    }
}
