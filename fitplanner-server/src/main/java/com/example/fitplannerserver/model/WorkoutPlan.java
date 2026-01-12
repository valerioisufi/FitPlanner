package com.example.fitplannerserver.model;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlan {
    private String title;
    private ArrayList<WorkoutSession> sessions;

    public void getCurrentSession() {


    }

    public List<WorkoutSession> getAllPlannedSessions() {
        return sessions;
    }

    public void addSession(int prevSessionIndex, WorkoutSession session) {
        sessions.add(prevSessionIndex, session);
    }
}
