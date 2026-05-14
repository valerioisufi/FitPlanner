package com.example.fitplannerserver.model.log;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionLog {
    private String notes;
    private SessionStatus status;
    private LocalDateTime date;

    private List<ExerciseLog> exerciseLogs;

    public SessionLog(String notes, SessionStatus status, LocalDateTime date) {
        this.notes = notes;
        this.status = status;
        this.date = date;

        this.exerciseLogs = new ArrayList<>();
    }

    public String getNotes() {
        return notes;
    }

    public String setNotes(String notes){
        return this.notes = notes;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public SessionStatus setSessionStatus(SessionStatus status){
        return this.status = status;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public LocalDateTime setDate(LocalDateTime date){
        return this.date = date;
    }



    public void addExerciseLog(ExerciseLog exerciseLog){
        this.exerciseLogs.add(exerciseLog);
    }

    public enum SessionStatus {
        COMPLETED,
        INTERRUPTED,
        SKIPPED
    }

}
