package com.example.fitplannerserver.model.plan;

import com.example.fitplannerserver.exception.WrongArgumentsException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WorkoutPlan {
    private final String planId;

    private String title;

    private LocalDate startDate;
    private int cycleLength;

    private Map<Integer, WorkoutSession> sessions;

    private String assignedToId;
    private String authorTrainerId;

    public WorkoutPlan(String planId, String title, int cycleLength) {
        this.planId = planId;

        this.title = title;
        this.cycleLength = cycleLength;

        this.sessions = new TreeMap<>();
    }


    public WorkoutPlan(WorkoutPlan old){
        this.planId = old.planId;
        this.title = old.title;

        this.cycleLength = old.cycleLength;
        this.startDate = old.startDate;

        this.sessions = new TreeMap<>();

        this.assignedToId = old.assignedToId;
        this.authorTrainerId = old.authorTrainerId;

        for (WorkoutSession session : old.sessions.values()){
            this.sessions.put(session.getDay(), new WorkoutSession(session));
        }
    }

    public WorkoutPlan(WorkoutPlan old, String newPlanId){
        this.planId = newPlanId;
        this.title = old.title;

        this.cycleLength = old.cycleLength;
        this.startDate = old.startDate;

        this.sessions = new TreeMap<>();

        this.authorTrainerId = old.authorTrainerId;

        for (WorkoutSession session : old.sessions.values()){
            this.sessions.put(session.getDay(), new WorkoutSession(session));
        }
    }

    public String getPlanId() {
        return planId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public WorkoutSession getSession(int day) {
        return this.sessions.get(day);
    }

    public List<WorkoutSession> getAllSessions(){
        return new ArrayList<>(this.sessions.values());
    }

    public void addSession(WorkoutSession newSession) {
        if(newSession.getDay() >= this.cycleLength) {
            throw new WrongArgumentsException("Session day non può essere maggiore della durata del ciclo di allenamento");
        }

        this.sessions.put(newSession.getDay(), newSession);
    }

    public void removeSession(int day) {
        this.sessions.remove(day);
    }

    public List<Integer> getSessionsDay(){
        return sessions.keySet().stream().sorted().toList();
    }

    public String getAssignedToId() {
        return this.assignedToId;
    }

    public void assignTo(String athleteId) {
        this.assignedToId = athleteId;
    }

    public String  getAuthorId() {
        return this.authorTrainerId;
    }

    public void setAuthorId(String trainerId) {
        this.authorTrainerId = trainerId;
    }

    public LocalDate getStartDate(){
        return this.startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public int getCycleLength() {
        return cycleLength;
    }

    public int calculateCurrentCycleDay(LocalDate targetDate) {
        if (this.startDate == null || targetDate.isBefore(this.startDate)) {
            return -1;
        }

        long daysElapsed = ChronoUnit.DAYS.between(this.startDate, targetDate);
        return (int) (daysElapsed % this.cycleLength);
    }

    // Restituisce la data di inizio del ciclo che contiene la targetDate
    public LocalDate calculateCycleStartDate(LocalDate targetDate) {
        long currentCycleIndex = calculateCurrentCycleIndex(targetDate);
        if (currentCycleIndex == -1) {
            return null;
        }

        return this.startDate.plusDays(currentCycleIndex * this.cycleLength);
    }

    public LocalDate calculateCycleEndDate(LocalDate targetDate) {
        long currentCycleIndex = calculateCurrentCycleIndex(targetDate);
        if (currentCycleIndex == -1) {
            return null;
        }

        return this.startDate.plusDays((currentCycleIndex + 1) * this.cycleLength);
    }

    private long calculateCurrentCycleIndex(LocalDate targetDate){
        if (this.startDate == null || targetDate.isBefore(this.startDate)) {
            return -1;
        }

        long daysElapsed = ChronoUnit.DAYS.between(this.startDate, targetDate);
        return daysElapsed / this.cycleLength;

    }

}
