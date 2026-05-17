package com.example.fitplannercommon;

import java.util.List;

public class SessionLogBean {
    private String userId;

    private String notes;
    private SessionStatus status;
    private long date;

    private String planIdReference;
    private int workoutSessionDay;

    private List<ExerciseLogBean> exerciseLogs;

    public SessionLogBean() {}

    public SessionLogBean(String userId, String notes, SessionStatus status, long date) {
        this.userId = userId;

        this.notes = notes;
        this.status = status;
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getPlanIdReference() {
        return planIdReference;
    }

    public void setPlanIdReference(String planIdReference) {
        this.planIdReference = planIdReference;
    }

    public int getWorkoutSessionDay() {
        return workoutSessionDay;
    }

    public void setWorkoutSessionDay(int workoutSessionDay) {
        this.workoutSessionDay = workoutSessionDay;
    }

    public List<ExerciseLogBean> getExerciseLogs() {
        return exerciseLogs;
    }

    public void setExerciseLogs(List<ExerciseLogBean> exerciseLogs) {
        this.exerciseLogs = exerciseLogs;
    }


    public enum SessionStatus {
        COMPLETED,
        SKIPPED,
        INTERRUPTED
    }
}
