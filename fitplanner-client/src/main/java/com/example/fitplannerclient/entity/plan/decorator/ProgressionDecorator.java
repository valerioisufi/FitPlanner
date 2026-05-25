package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;

import java.util.List;

public class ProgressionDecorator extends FlowDecorator {
    private List<Integer> progression;

    public ProgressionDecorator(PlanNode wrappedNode) {
        super(wrappedNode);
    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        // TODO: implementare la logica di progressione, ad esempio incrementando i carichi o le ripetizioni in base alla progressione
        return null;
    }

    @Override
    public void reset() {

    }

    public String getProgression() {
        StringBuilder progressionString = new StringBuilder();
        if (progression != null) {
            for (int integer : progression) {
                progressionString.append(integer).append(",");
            }
        }

        return progressionString.toString();
    }

    @Override
    public ProgressionDecorator cloneWithNode(PlanNode newWrappedNode) {
        ProgressionDecorator copy = new ProgressionDecorator(newWrappedNode);
        copy.progression = this.progression;
        return copy;
    }
}
