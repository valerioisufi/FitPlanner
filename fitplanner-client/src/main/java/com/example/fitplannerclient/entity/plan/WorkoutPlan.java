package com.example.fitplannerclient.entity.plan;

import com.example.fitplannerclient.controller.plan.AcceptWorkoutPlanVisitor;
import com.example.fitplannerclient.controller.plan.WorkoutPlanVisitor;
import com.example.fitplannerclient.util.IDGenerator;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlan implements AcceptWorkoutPlanVisitor {
    private String name;
    private String id;

    private List<WorkoutSession> sessions;

    public WorkoutPlan(String name, String id) {
        this.name = name;
        this.id = id;
        this.sessions = new ArrayList<>();
    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    public WorkoutPlan(String name){
        this(name, IDGenerator.generateUUID());
    }

    public void changeName(String newName){
        this.name = newName;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public List<WorkoutSession> getSessions() {
        return sessions;
    }

    public void addSession(WorkoutSession session) {
        this.sessions.add(session);
    }

    public void removeSession(String sessionId){
        for (WorkoutSession session : sessions) {
            if (session.getId().equals(sessionId)) {
                sessions.remove(session);
                return;
            }
        }
    }
}
