package com.example.fitplannerclient.entity.plan.context;

import java.util.HashMap;

public class ExecutionContext {
    private final HashMap<String, String> parameters;
    private ControlSignal currentSignal;
    private int tickDelta;

    public ExecutionContext() {
        this.parameters = new HashMap<>();
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

    public ControlSignal getCurrentSignal() {
        return currentSignal;
    }

    public void setTickDelta(int tickDelta) {
        this.tickDelta = tickDelta;
    }
    
    public int getTickDelta() {
        return tickDelta;
    }

}
