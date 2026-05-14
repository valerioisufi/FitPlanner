package com.example.fitplannerserver.model.plan;

import com.example.fitplannercommon.WorkoutState;

public class WorkoutSession {
    private final String sessionId;

    private String title;
    private String content;

    private int day;
    private WorkoutState state;

    public WorkoutSession(String sessionId, String title, String content, int day, WorkoutState state) {
        this.sessionId = sessionId;

        this.title = title;
        this.content = content;

        this.day = day;
        this.state= state;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getTitle() {
        return title;
    }

    public int getDay() {
        return day;
    }

    public WorkoutState getState() {
        return state;
    }

    public void setState(WorkoutState state) {
        this.state = state;
    }

}