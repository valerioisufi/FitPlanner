package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressionDecorator extends LoopDecorator {
    private Map<String, List<String>> progressions;

    public ProgressionDecorator(PlanNode wrappedNode) {
        super(wrappedNode, 0);
    }

    public void setProgressions(Map<String, List<String>> progressions) {
        this.progressions = progressions;
        int maxRounds = 0;

        if (progressions != null) {

            for (List<String> values : progressions.values()) {
                if (values != null && values.size() > maxRounds) {
                    maxRounds = values.size();
                }
            }
        }

        this.setRounds(maxRounds);
    }

    public Map<String, List<String>> getProgressions() {
        return progressions;
    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        if (progressions != null) {

            for (Map.Entry<String, List<String>> entry : progressions.entrySet()) {
                List<String> values = entry.getValue();

                if (values != null && currentRound < values.size()) {
                    context.setParameter(entry.getKey(), values.get(currentRound));
                }
            }

        }
        
        return super.execute(context);
    }

    @Override
    public ProgressionDecorator cloneWithNode(PlanNode newWrappedNode) {
        ProgressionDecorator copy = new ProgressionDecorator(newWrappedNode);

        if (this.progressions != null) {
            Map<String, List<String>> clonedMap = new HashMap<>();

            for (Map.Entry<String, List<String>> entry : this.progressions.entrySet()) {
                clonedMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }

            copy.setProgressions(clonedMap);
        }

        return copy;
    }

}
