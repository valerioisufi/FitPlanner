package com.example.fitplannerclient.entity.plan.context;

import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExecutionContext {
    private final Map<String, String> parameters = new HashMap<>();
    private ControlSignal currentSignal;

    private static final Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");

    private int tickDelta;
    private ExerciseNode activeNode;

    public ExecutionContext() {
    }

    public void setActiveNode(ExerciseNode node) {
        this.activeNode = node;
    }

    public ExerciseNode getActiveNode() {
        return this.activeNode;
    }

    public void reset() {
        this.currentSignal = ControlSignal.NONE;
        this.tickDelta = 0;
        this.activeNode = null;
    }

    public void setParameter(String key, String value) {
        parameters.put(key, value);
    }

    public String getParameterValueIfExist(String key) {
        return parameters.get(key);
    }

    public void injectSignal(ControlSignal signal){
        this.currentSignal = signal;
    }

    public boolean consumeSignal(ControlSignal signal){
        if(this.currentSignal == signal) {
            this.currentSignal = ControlSignal.NONE;
            return true;
        }
        return false;
    }

    public ControlSignal getCurrentSignal() {
        return currentSignal;
    }

    public void setTickDelta(int tickDelta) {
        this.tickDelta = tickDelta;
    }
    
    public int getTickDelta() {
        return tickDelta;
    }

    public void consumeTickDelta(int amount){
        this.tickDelta = Math.max(0, this.tickDelta - amount);
    }

    public String resolveVariables(String input) {
        if (input == null || !input.contains("${")) {
            return input;
        }

        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();

        // trova ogni blocco ${ } nella stringa
        while (matcher.find()) {
            String key = matcher.group(1); // estrae il nome della variabile
            String value = parameters.getOrDefault(key, matcher.group(0));

            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public int resolveAsInteger(String expression, int fallback) {
        if (expression != null && !expression.isEmpty()) {
            try {
                return Integer.parseInt(resolveVariables(expression));
            } catch (NumberFormatException e) {
                return fallback;
            }
        }

        return fallback;
    }
}
