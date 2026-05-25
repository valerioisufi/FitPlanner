package com.example.fitplannerclient.entity.plan.context;

import java.util.HashMap;

public class ExecutionContext {
    private final HashMap<String, String> parameters = new HashMap<>();
    private ControlSignal currentSignal;

    private int tickDelta;

    public ExecutionContext() {
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

}
