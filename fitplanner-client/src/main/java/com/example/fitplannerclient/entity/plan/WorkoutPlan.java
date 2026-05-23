package com.example.fitplannerclient.entity.plan;

import com.example.fitplannerclient.controller.plan.visitor.AcceptWorkoutPlanVisitor;
import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlan implements AcceptWorkoutPlanVisitor {
    private String name;
    private String planId;

    private int cycleLength;

    private final List<WorkoutSession> sessions = new ArrayList<>();
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

    public List<WorkoutSession> getSessions() {
        return sessions;
    }

    public void addSession(WorkoutSession session) {
        this.sessions.add(session);
    }

    public void removeSession(int sessionDay){
        for (WorkoutSession session : sessions) {
            if (session.getDay() == sessionDay) {
                sessions.remove(session);
                return;
            }
        }
    }

    public int getCycleLength() {
        return cycleLength;
    }

    public void setCycleLength(int cycleLength) {
        this.cycleLength = cycleLength;
    }
}
