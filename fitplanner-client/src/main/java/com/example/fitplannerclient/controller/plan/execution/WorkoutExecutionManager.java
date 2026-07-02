package com.example.fitplannerclient.controller.plan.execution;

import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import com.example.fitplannerclient.bean.log.ExerciseLogBean;
import com.example.fitplannerclient.bean.log.ExerciseSetBean;
import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.entity.log.ExerciseLog;
import com.example.fitplannerclient.entity.log.SessionLog;
import com.example.fitplannerclient.repository.WorkoutPlanRepository;
import com.example.fitplannerclient.controller.plan.execution.engine.WorkoutEngine;
import com.example.fitplannerclient.controller.plan.execution.engine.WorkoutEngineImpl;
import com.example.fitplannerclient.controller.plan.execution.observer.WorkoutExecutionObserver;
import com.example.fitplannerclient.controller.plan.execution.observer.WorkoutExecutionSubject;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.execution.WorkoutStatus;
import com.example.fitplannerclient.repository.ExerciseRepository;
import com.example.fitplannerclient.repository.SessionLogRepository;
import com.example.fitplannerclient.controller.plan.mapper.PlanToBeanVisitor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WorkoutExecutionManager {

    private final SessionLogRepository logRepository;
    private final WorkoutPlanRepository planRepository;
    private final ExerciseRepository exerciseRepository;

    private WorkoutEngine engine;

    private String currentPlanId;
    private WorkoutSession currentSession;

    private WorkoutPlan currentPlan;

    private WorkoutExecutionSubject workoutExecutionSubject = new WorkoutExecutionSubject();
    private String lastActiveNodeId = null;
    private int currentAbsoluteSessionDay;

    public WorkoutExecutionManager(WorkoutPlanRepository planRepository, SessionLogRepository logRepository, ExerciseRepository exerciseRepository) {
        this.planRepository = planRepository;
        this.logRepository = logRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public void attachObserver(WorkoutExecutionObserver observer) {
        workoutExecutionSubject.attach(observer);
    }

    public void detachObserver(WorkoutExecutionObserver observer) {
        workoutExecutionSubject.detach(observer);
    }

    public CompletableFuture<Void> startSessionAsync(String planId, int absoluteSessionDay) {
        this.currentPlanId = planId;
        this.currentAbsoluteSessionDay = absoluteSessionDay;

        // Recupera il piano direttamente tramite il repository (sfrutta la cache se possibile)
        return planRepository.getAssignedPlanAsync()
                .thenAccept(plan -> {
                    if (plan == null) throw new IllegalStateException("Nessun piano assegnato");
                    this.currentPlan = plan;

                    int relativeDay = absoluteSessionDay % plan.getCycleLength();
                    WorkoutSession targetSession = findTargetSession(plan, relativeDay);
                    if (targetSession == null) {
                        throw new IllegalArgumentException("Sessione non trovata per il giorno specificato");
                    }
                    this.currentSession = targetSession;

                    // Inizializza il motore passando la radice della sessione (Entity)
                    this.engine = new WorkoutEngineImpl(this.currentSession.getRoot());

                    // Inizializza la callback funzionale (Clean Architecture)
                    setupEngineUpdateListener();
                });
    }

    private WorkoutSession findTargetSession(WorkoutPlan plan, int workoutSessionDay) {
        return plan.getSessions().stream()
                .filter(s -> s.getDay() == workoutSessionDay)
                .findFirst()
                .orElse(null);
    }

    private void setupEngineUpdateListener() {
        this.engine.setOnUpdateListener((state, activeNode, timeRemaining) -> {
            WorkoutStatus status = (state != null) ? state.getStatus() : WorkoutStatus.UNKNOWN;
            
            WorkoutExecutionObserver.WorkoutExecutionState observerState = WorkoutExecutionObserver.WorkoutExecutionState.STOPPED;
            if (status == WorkoutStatus.PLAYING) observerState = WorkoutExecutionObserver.WorkoutExecutionState.PLAYING;
            else if (status == WorkoutStatus.PAUSED) observerState = WorkoutExecutionObserver.WorkoutExecutionState.PAUSED;
            
            workoutExecutionSubject.notifyCurrentWorkoutEngineState(observerState);
            
            if (timeRemaining > 0) {
                workoutExecutionSubject.notifyCurrentRestTime(timeRemaining / 1000);
            }
            
            if (activeNode != null && !activeNode.getId().equals(lastActiveNodeId)) {
                lastActiveNodeId = activeNode.getId();
                String resourceId = activeNode.getResourceId();
                if (resourceId != null && !resourceId.isEmpty()) {
                    fetchAndNotifyExercise(resourceId);
                }
            }
        });
    }

    private void fetchAndNotifyExercise(String resourceId) {
        exerciseRepository.getExercisesAsync(List.of(resourceId)).thenAccept(list -> {
            if (!list.isEmpty()) {
                var entity = list.getFirst();
                ExerciseDescriptionBean bean = new ExerciseDescriptionBean();
                bean.setExerciseId(entity.getExerciseId());
                bean.setName(entity.getName());
                bean.setExecution(entity.getExecution());
                bean.setMuscleGroups(entity.getMuscleGroups());
                workoutExecutionSubject.notifyCurrentExercise(bean);
            }
        });
    }

    public PlanNodeBean getSessionRootBeanForUi() {
        if (currentSession == null || currentSession.getRoot() == null) return null;
        PlanToBeanVisitor visitor = new PlanToBeanVisitor(uuid -> {
            if (exerciseRepository != null) {
                var ex = exerciseRepository.getCachedExercise(uuid);
                if (ex != null) return ex.getName();
            }
            return "Esercizio Sconosciuto";
        });
        currentSession.getRoot().accept(visitor);
        return visitor.getCurrentPlanNodeBean();
    }

    public WorkoutSession getCurrentSession() {
        return currentSession;
    }

    public void play() {
        if (engine != null) engine.play();
    }

    public void pause() {
        if (engine != null) engine.pause();
    }

    public void stop() {
        if (engine != null) engine.stop();
    }

    public void skipNext() {
        if (engine != null) engine.skipNext();
    }

    public void skipPrevious() {
        if (engine != null) engine.skipPrevious();
    }

    public void done() {
        if (engine != null) engine.done();
    }

    public CompletableFuture<ExerciseLogBean> getLastWeightUsedAsync(String exerciseId) {
        return logRepository.getLastWeightUsedAsync(exerciseId)
                .thenApply(this::exerciseLogEntityToBean);
    }

    public CompletableFuture<Void> finishAndSaveSession() {
        if (engine != null) {
            engine.stop();
        }

        String status = "COMPLETED";
        if (engine != null && engine.getState() != null && engine.getState().getStatus() != WorkoutStatus.STOPPED) {
            status = "INTERRUPTED";
        }

        SessionLog log = new SessionLog(System.currentTimeMillis());
        log.updateNotes("Sessione completata tramite App");
        log.setStatus(status);
        log.setPlanId(currentPlanId);
        log.setWorkoutSessionDay(currentAbsoluteSessionDay);
        // TODO: aggiungere i risultati dall'ExecutionContext tramite log.addLog(...)

        return logRepository.saveSessionLogAsync(log);
    }

    private ExerciseLogBean exerciseLogEntityToBean(ExerciseLog entity) {
        if (entity == null) return null;
        List<ExerciseSetBean> sets = entity.getSets().stream()
                .map(set -> new ExerciseSetBean(set.reps(), set.load(), set.rpe()))
                .toList();

        return new ExerciseLogBean(entity.getName(), entity.getExerciseId(), sets, entity.getNotes());
    }
}
