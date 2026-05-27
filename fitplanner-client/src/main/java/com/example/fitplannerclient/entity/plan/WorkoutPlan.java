package com.example.fitplannerclient.entity.plan;

import com.example.fitplannerclient.controller.plan.visitor.AcceptWorkoutPlanVisitor;
import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WorkoutPlan implements AcceptWorkoutPlanVisitor {
    private String name;
    private String planId;

    private int cycleLength;

    private final Map<Integer, WorkoutSession> sessions = new TreeMap<>();
    private WorkoutSession currentSession;

    public WorkoutPlan(String name, String planId) {
        this.name = name;
        this.planId = planId;
    }

    public WorkoutPlan(String name){
        this.name = name;
    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    public void changeName(String newName){
        this.name = newName;
    }

    public String getName() {
        return name;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }


    public void addSession(WorkoutSession session) {
        this.sessions.put(session.getDay(), session);
    }

    public WorkoutSession removeSession(int sessionDay) {
        return this.sessions.remove(sessionDay);
    }

    public List<WorkoutSession> getSessions() {
        return new ArrayList<>(sessions.values());
    }

    public int getCycleLength() {
        return cycleLength;
    }

    public void setCycleLength(int cycleLength) {
        this.cycleLength = cycleLength;
    }
}
