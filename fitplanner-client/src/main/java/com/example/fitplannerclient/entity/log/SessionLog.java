package com.example.fitplannerclient.entity.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionLog {
    private String userId;

    private String notes;
    private long date;
    private int workoutSessionDay;
    private String planId;
    private String status;

    private final Map<String, ExerciseLog> exerciseLogs = new HashMap<>();

    public SessionLog(long date) {
        this.date = date;
    }

    public double calculateTotalSessionVolume() {
        return exerciseLogs.values().stream()
                .mapToDouble(ExerciseLog::calculateTotalVolume)
                .sum();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public long getDate() { return date; }

    public int getWorkoutSessionDay() { return workoutSessionDay; }
    public void setWorkoutSessionDay(int workoutSessionDay) { this.workoutSessionDay = workoutSessionDay; }

    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }

    public void updateNotes(String notes) {
        this.notes = notes;
    }

    public void addLog(ExerciseLog log) {
        ExerciseLog currentExerciseLog = exerciseLogs.get(log.getExerciseId());

        if (currentExerciseLog == null) {
            exerciseLogs.put(log.getExerciseId(), log);
        } else {
            currentExerciseLog.addSets(log.getSets());

            if(log.getNotes() != null && !log.getNotes().isEmpty()) {
                currentExerciseLog.updateNotes(log.getNotes());
            }
        }

    }

    public List<ExerciseLog> getExerciseLogs() {
        return exerciseLogs.values().stream().toList();
    }


}