package com.example.fitplannerclient.controller.plan.editor.command;

import com.example.fitplannerclient.controller.plan.core.visitor.EmpyWorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.decorator.*;

public class UpdateDecoratorValueCommand implements WorkoutPlanEditorCommand {

    private FlowDecorator flowDecorator;

    private String previousValue;
    private String newValue;

    public UpdateDecoratorValueCommand(FlowDecorator flowDecorator, String newValue) {
        this.flowDecorator = flowDecorator;
        this.newValue = newValue;
    }

    @Override
    public void execute() {
        flowDecorator.accept(new EmpyWorkoutPlanVisitor() {
            @Override
            public void visit(RestDecorator restDecorator) {
                previousValue = restDecorator.getRestDuration();
                restDecorator.setRestDuration(newValue);
            }

            @Override
            public void visit(LoopDecorator loopDecorator) {
                previousValue = loopDecorator.getRoundsExpression();
                loopDecorator.setRoundsExpression(newValue);
            }

            @Override
            public void visit(TimeLimitDecorator timeLimitDecorator) {
                previousValue = timeLimitDecorator.getTimeLimit();
                timeLimitDecorator.setTimeLimit(newValue);
            }

            @Override
            public void visit(IntervalDecorator intervalDecorator) {
                previousValue = intervalDecorator.getIntervalDuration();
                intervalDecorator.setIntervalDuration(newValue);
            }

            @Override
            public void visit(ProgressionDecorator progressionDecorator) {
                previousValue = progressionDecorator.getProgressionString();
                progressionDecorator.setProgressionString(newValue);
            }
        });
    }

    @Override
    public void undo() {
        flowDecorator.accept(new EmpyWorkoutPlanVisitor() {
            @Override
            public void visit(RestDecorator restDecorator) {
                restDecorator.setRestDuration(previousValue);
            }

            @Override
            public void visit(LoopDecorator loopDecorator) {
                loopDecorator.setRoundsExpression(previousValue);
            }

            @Override
            public void visit(TimeLimitDecorator timeLimitDecorator) {
                timeLimitDecorator.setTimeLimit(previousValue);
            }

            @Override
            public void visit(IntervalDecorator intervalDecorator) {
                intervalDecorator.setIntervalDuration(previousValue);
            }

            @Override
            public void visit(ProgressionDecorator progressionDecorator) {
                progressionDecorator.setProgressionString(previousValue);
            }
        });
    }
}
