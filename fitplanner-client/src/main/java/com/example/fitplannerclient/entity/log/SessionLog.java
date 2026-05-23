package com.example.fitplannerclient.entity.log;

import java.util.List;

public class SessionLog {
    private final long date;
    private final List<ExerciseLog> exerciseLogs;

    // Altri campi come status, userId, etc...

    public SessionLog(long date, List<ExerciseLog> exerciseLogs) {
        this.date = date;
        this.exerciseLogs = exerciseLogs;
    }

    public double calculateTotalSessionVolume() {
        return exerciseLogs.stream().mapToDouble(ExerciseLog::calculateTotalVolume).sum();
    }
    
    public long getDate() { return date; }
    public List<ExerciseLog> getExerciseLogs() { return exerciseLogs; }
}