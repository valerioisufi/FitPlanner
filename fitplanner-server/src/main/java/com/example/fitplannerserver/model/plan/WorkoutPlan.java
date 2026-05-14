package com.example.fitplannerserver.model.plan;

import com.example.fitplannercommon.WorkoutState;

import java.util.*;

public class WorkoutPlan {
    private final String planUuid;

    private String title;
    private Map<Integer, WorkoutSession> sessions;

    private String assignedToId;
    private String authorTrainerId;

    public WorkoutPlan(String planUuid, String title) {
        this.planUuid = planUuid;

        this.title = title;
        this.sessions = new TreeMap<>();
    }

    public String getPlanUuid() {
        return planUuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public WorkoutSession getToDoSession() {
        for (WorkoutSession session : sessions.values()){
            if (session.getState() == WorkoutState.TO_DO || session.getState() == WorkoutState.IN_PROGRESS ){
                return session;
            }
        }
        return null;
    }


    public WorkoutSession getSession(int day) {
        return this.sessions.get(day);
    }

    public void addSession(WorkoutSession newSession) {
        this.sessions.put(newSession.getDay(), newSession);
    }

    public String getAssignedToId() {
        return this.assignedToId;
    }

    public void assignTo(String athleteId) {
        this.assignedToId = athleteId;
    }

    public void removeSession(int day) {
        this.sessions.remove(day);
    }

}
