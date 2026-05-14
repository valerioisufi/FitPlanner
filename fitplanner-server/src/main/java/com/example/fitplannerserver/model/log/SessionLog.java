package com.example.fitplannerserver.model.log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionLog {
    private String userId;

    private String notes;
    private SessionStatus status;
    private LocalDateTime date;

    private List<ExerciseLog> exerciseLogs;

    public SessionLog(String userId, String notes, SessionStatus status, LocalDateTime date) {
        this.userId = userId;
        this.notes = notes;
        this.status = status;
        this.date = date;

        this.exerciseLogs = new ArrayList<>();
    }

    public SessionLog(SessionLog other) {
        this.userId = other.userId;

        this.notes = other.notes;
        this.status = other.status;
        this.date = other.date;

        this.exerciseLogs = new ArrayList<>();
        if (other.exerciseLogs != null) {
            for (ExerciseLog exerciseLog : other.exerciseLogs) {
                this.exerciseLogs.add(new ExerciseLog(exerciseLog));
            }
        }
    }

    public String getUserId() {
        return userId;
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

    public void setSessionStatus(SessionStatus status) {
        this.status = status;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<ExerciseLog> getExerciseLogs() {
        return exerciseLogs;
    }

    public void addExerciseLog(ExerciseLog exerciseLog) {
        this.exerciseLogs.add(exerciseLog);
    }

    public enum SessionStatus {
        COMPLETED,
        INTERRUPTED,
        SKIPPED
    }
}