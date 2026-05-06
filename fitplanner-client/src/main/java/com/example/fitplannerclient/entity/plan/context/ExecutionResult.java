package com.example.fitplannerclient.entity.plan.context;

public class ExecutionResult {
    private PlanNodeState state;
    private int requestedSleepMillis;

    public ExecutionResult(PlanNodeState state, int requestedSleepMillis){
        this.state = state;
        this.requestedSleepMillis = requestedSleepMillis;
    }

    public PlanNodeState getState(){
        return state;
    }

    public int getRequestedSleepMillis(){
        return requestedSleepMillis;
    }
}
