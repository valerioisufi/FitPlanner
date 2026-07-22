package com.example.fitplannerclient.entity.plan.decorator;

import com.example.fitplannerclient.entity.plan.execution.PlanNodeState;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.execution.ExecutionContext;
import com.example.fitplannerclient.entity.plan.execution.ExecutionResult;

import java.util.*;

public class ProgressionDecorator extends LoopDecorator {
    private String progressionString;
    private Map<String, List<String>> parsedProgressions;

    public ProgressionDecorator(PlanNode wrappedNode, String progressionString) {
        super(wrappedNode, "0");
        this.progressionString = progressionString;
    }

    public static Map<String, List<String>> parseProgressions(String str) {
        if (str == null || str.trim().isEmpty()) {
            return Map.of();
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

        return result.isEmpty() ? Map.of() : result;
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        if (this.state == PlanNodeState.IDLE) {
            String resolvedString = context.resolveVariables(this.progressionString);
            this.parsedProgressions = parseProgressions(resolvedString);
            
            int maxRounds = 0;
            for (List<String> values : this.parsedProgressions.values()) {
                if (values != null && values.size() > maxRounds) {
                    maxRounds = values.size();
                }
            }
            this.roundsExpression = String.valueOf(maxRounds);
        }

        for (Map.Entry<String, List<String>> entry : parsedProgressions.entrySet()) {
            List<String> values = entry.getValue();

            if (values != null && currentRound < values.size()) {
                context.setParameter(entry.getKey(), values.get(currentRound));
            }
        }
        
        return super.execute(context);
    }


    @Override
    public void setValue(String value) {
        this.progressionString = value;
    }

    @Override
    public FlowDecoratorType getType() {
        return FlowDecoratorType.PROGRESSION;
    }

    @Override
    public String getSerializedValue() {
        return this.progressionString;
    }

    @Override
    public ProgressionDecorator cloneWithNode(PlanNode newWrappedNode) {
        return new ProgressionDecorator(newWrappedNode, this.progressionString);
    }
}
