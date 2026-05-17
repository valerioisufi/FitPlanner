package com.example.fitplannerserver.model.log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionLog {
    private final String userId;

    private String notes;
    private SessionStatus status;
    private LocalDateTime date;

    private PlanReference planReference;

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

        this.planReference = new PlanReference(
                other.planReference.planId,
                other.planReference.workoutSessionDay
        );

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

    public String getPlanId(){
        return this.planReference.planId;
    }

    public int getWorkoutSessionDay(){
        return this.planReference.workoutSessionDay;
    }

    public List<ExerciseLog> getExerciseLogs() {
        return exerciseLogs;
    }

    public void addExerciseLog(ExerciseLog exerciseLog) {
        this.exerciseLogs.add(exerciseLog);
    }

    private record PlanReference(
            String planId,

            // giorno del WorkoutSession, a partire dalla data di inizio del piano
            // a cui questo SessionLog fa riferimento
            int workoutSessionDay
    ){}

    public enum SessionStatus {
        COMPLETED,
        INTERRUPTED,
        SKIPPED
    }
}