package com.example.fitplannercommon;

import java.util.List;

public class SessionLogDTO {
    private String userId;

    private String notes;
    private SessionStatus status;
    private long date;

    private String planIdReference;
    private int workoutSessionDay;

    private List<ExerciseLogDTO> exerciseLogs;

    public SessionLogDTO() {}

    public SessionLogDTO(String userId, String notes, SessionStatus status, long date, String planIdReference, int workoutSessionDay, List<ExerciseLogDTO>
                         exerciseLogs) {
        this.userId = userId;
        this.notes = notes;
        this.status = status;
        this.date = date;
        this.planIdReference = planIdReference;
        this.workoutSessionDay = workoutSessionDay;
        this.exerciseLogs = exerciseLogs;
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

    public List<ExerciseLogDTO> getExerciseLogs() {
        return exerciseLogs;
    }

    public void setExerciseLogs(List<ExerciseLogDTO> exerciseLogs) {
        this.exerciseLogs = exerciseLogs;
    }


    public enum SessionStatus {
        COMPLETED,
        SKIPPED,
        INTERRUPTED
    }
}
