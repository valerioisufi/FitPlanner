package com.example.fitplannerserver.model;

import com.example.fitplannercommon.WorkoutState;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlan {
    private String title;
    private ArrayList<WorkoutSession> sessions;
    private Boolean completed = false;
    private

    public WorkoutPlan(String title) {
        this.title = title;
        sessions = new ArrayList<>();
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public WorkoutSession getCurrentSession() {
        for (WorkoutSession session : sessions){
            if (session.getState() == WorkoutState.TO_DO || session.getState() == WorkoutState.IN_PROGRESS ){
                return session;
            }
        }
        return null;
    }



    public List<WorkoutSession> getAllPlannedSessions() {
        return sessions;
    }

    public void addSession(int prevSessionIndex, WorkoutSession session) {
        sessions.add(prevSessionIndex, session);
    }
    public void addSession(WorkoutSession session) {
        sessions.add(session);
    }
}
