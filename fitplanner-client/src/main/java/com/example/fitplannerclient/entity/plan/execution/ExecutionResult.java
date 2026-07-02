package com.example.fitplannerclient.entity.plan.execution;

public class ExecutionResult {
    private final PlanNodeState state;
    private final int requestedSleepMillis;

    public ExecutionResult(PlanNodeState state, int requestedSleepMillis){
        this.state = state;

        if(requestedSleepMillis < 0) requestedSleepMillis = -1;
        this.requestedSleepMillis = requestedSleepMillis;
    }

    public ExecutionResult(PlanNodeState state){
        this(state, -1);
    }

    public PlanNodeState getState(){
        return state;
    }

    public int getRequestedSleepMillis(){
        return requestedSleepMillis;
    }
}
