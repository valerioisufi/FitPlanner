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
    private String progressionString;
    private Map<String, List<String>> parsedProgressions;

    public ProgressionDecorator(PlanNode wrappedNode, String progressionString) {
        super(wrappedNode, 0);
        this.setProgressionString(progressionString);
    }

    public void setProgressionString(String progressionString) {
        this.progressionString = progressionString;
        this.parsedProgressions = parseProgressions(progressionString);
        
        int maxRounds = 0;
        if (this.parsedProgressions != null) {
            for (List<String> values : this.parsedProgressions.values()) {
                if (values != null && values.size() > maxRounds) {
                    maxRounds = values.size();
                }
            }
        }

        this.setRounds(maxRounds);
    }

    public String getProgressionString() {
        return progressionString;
    }

    private Map<String, List<String>> parseProgressions(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }

        // la stringa da parsare è del tipo
        // WEIGHT: 50, 52.5, 55; REPS: 10, 8, 6
        Map<String, List<String>> result = new HashMap<>();
        String[] rules = str.split(";");

        for (String rule : rules) {
            String[] parts = rule.split(":", 2);

            if (parts.length == 2) {
                String varName = parts[0].trim();
                String[] valuesArray = parts[1].split(",");

                List<String> valuesList = new ArrayList<>();

                for (String val : valuesArray) {
                    valuesList.add(val.trim());
                }

                result.put(varName, valuesList);
            }
        }

        return result.isEmpty() ? null : result;
    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        if (parsedProgressions != null) {
            for (Map.Entry<String, List<String>> entry : parsedProgressions.entrySet()) {
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
        return new ProgressionDecorator(newWrappedNode, this.progressionString);
    }
}
