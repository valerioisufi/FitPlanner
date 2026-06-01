package com.example.fitplannerclient.bean.log;

public class SessionLogBean {
    private final String athleteId;
    private final int workoutSessionDay;
    private final long date;
    private final String status;
    private final String planId;

    public SessionLogBean(String athleteId, int workoutSessionDay, long date, String status, String planId) {
        this.athleteId = athleteId;
        this.workoutSessionDay = workoutSessionDay;
        this.date = date;
        this.status = status;
        this.planId = planId;
    }

    public String getAthleteId() { return athleteId; }
    public int getWorkoutSessionDay() { return workoutSessionDay; }
    public long getDate() { return date; }
    public String getStatus() { return status; }
    public String getPlanId() { return planId; }
}
