package com.example.fitplannercommon;

import java.time.LocalDateTime;

public class SessionLogBean {
    private String notes;
    private SessionStatus status;
    private LocalDateTime date;

    public SessionLogBean(String notes, SessionStatus status, LocalDateTime date) {
        this.notes = notes;
        this.status = status;
        this.date = date;
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }


    public enum SessionStatus {
        COMPLETED,
        SKIPPED,
        INTERRUPTED
    }
}
