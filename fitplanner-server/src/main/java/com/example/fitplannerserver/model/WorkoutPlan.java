package com.example.fitplannerserver.model;

import com.example.fitplannercommon.WorkoutState;

import java.lang.reflect.Array;
import java.util.*;

public class WorkoutPlan {
    private String title;
    private Map<Integer, WorkoutSession> sessions;
    private Athlete assignedTo;

    public WorkoutPlan(String title) {
        this.title = title;
        this.sessions = new TreeMap<>();
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

    public Athlete getAssignedTo() {
        return this.assignedTo;
    }

    public void assignTo(Athlete athlete) {
        this.assignedTo = athlete;
    }

    public void removeSession(int day) {
        this.sessions.remove(day);
    }

}
