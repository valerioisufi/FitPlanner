package com.example.fitplannerclient.controller.plan.execution;

import com.example.fitplannerclient.bean.exercise.CurrentExerciseBean;
import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import com.example.fitplannerclient.bean.log.ExerciseLogBean;
import com.example.fitplannerclient.bean.log.ExerciseSetBean;
import com.example.fitplannerclient.bean.plan.ExerciseModifierBean;
import com.example.fitplannerclient.entity.ExerciseDescription;
import com.example.fitplannerclient.entity.log.ExerciseLog;
import com.example.fitplannerclient.entity.log.ExerciseSet;
import com.example.fitplannerclient.entity.log.SessionLog;
import com.example.fitplannerclient.entity.plan.execution.ExecutionResult;
import com.example.fitplannerclient.repository.WorkoutPlanRepository;
import com.example.fitplannerclient.controller.plan.execution.engine.WorkoutEngine;
import com.example.fitplannerclient.controller.plan.execution.engine.WorkoutEngineImpl;
import com.example.fitplannerclient.controller.plan.execution.observer.WorkoutExecutionObserver;
import com.example.fitplannerclient.controller.plan.execution.observer.WorkoutExecutionSubject;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.execution.PlanNodeState;
import com.example.fitplannerclient.entity.plan.execution.WorkoutStatus;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import com.example.fitplannerclient.repository.ExerciseRepository;
import com.example.fitplannerclient.repository.SessionLogRepository;
import com.example.fitplannerclient.util.IDGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WorkoutExecutionManager {

    private final SessionLogRepository logRepository;
    private final WorkoutPlanRepository planRepository;
    private final ExerciseRepository exerciseRepository;

    private WorkoutEngine engine;

    private String currentPlanId;
    private WorkoutSession currentSession;

    private final WorkoutExecutionSubject workoutExecutionSubject = new WorkoutExecutionSubject();
    private String lastActiveNodeId = null;
    private WorkoutExecutionObserver.WorkoutExecutionPhase lastPhase = null;
    private int currentAbsoluteSessionDay;

    private SessionLog currentSessionLog;

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

        // Recupera il piano tramite il repository (sfrutta la cache se possibile)
        return planRepository.getAssignedPlanAsync()
                .thenAccept(plan -> {
                    if (plan == null) throw new IllegalStateException("Nessun piano assegnato");

                    int cycleDay = absoluteSessionDay % plan.getCycleLength();
                    WorkoutSession targetSession = findTargetSession(plan, cycleDay);
                    if (targetSession == null) {
                        throw new IllegalArgumentException("Sessione non trovata per il giorno specificato");
                    }
                    this.currentSession = targetSession;

                    // Log di sessione accumulato durante l'esecuzione
                    this.currentSessionLog = new SessionLog(System.currentTimeMillis());
                    this.lastActiveNodeId = null;
                    this.lastPhase = null;

                    // Inizializza il motore passando la radice della sessione
                    this.engine = new WorkoutEngineImpl(this.currentSession.getRoot());

                    setupEngineUpdateListener();
                });
    }

    private WorkoutSession findTargetSession(WorkoutPlan plan, int cycleDay) {
        return plan.getSessions().stream()
                .filter(s -> s.getDay() == cycleDay)
                .findFirst()
                .orElse(null);
    }

    private void setupEngineUpdateListener() {
        this.engine.setOnUpdateListener((status, result, activeNode, breadcrumb) -> {
            notifyEngineStatus(status);
            notifyExecutionPhase(result);
            notifyActiveNodeChange(activeNode, breadcrumb);
        });
    }

    private void notifyEngineStatus(WorkoutStatus status) {
        WorkoutExecutionObserver.WorkoutExecutionState observerState = WorkoutExecutionObserver.WorkoutExecutionState.STOPPED;
        if (status == WorkoutStatus.PLAYING) {
            observerState = WorkoutExecutionObserver.WorkoutExecutionState.PLAYING;
        } else if (status == WorkoutStatus.PAUSED) {
            observerState = WorkoutExecutionObserver.WorkoutExecutionState.PAUSED;
        }
        workoutExecutionSubject.notifyCurrentWorkoutEngineState(observerState);
    }

    private void notifyExecutionPhase(ExecutionResult result) {
        WorkoutExecutionObserver.WorkoutExecutionPhase phase = mapToPhase(result.getState());

        if (phase != null && phase != lastPhase) {
            lastPhase = phase;
            workoutExecutionSubject.notifyExecutionPhase(phase);
        }

        if (phase == WorkoutExecutionObserver.WorkoutExecutionPhase.REST && result.getRequestedSleepMillis() > 0) {
            workoutExecutionSubject.notifyCurrentRestTime(result.getRequestedSleepMillis() / 1000);
        }
    }

    private void notifyActiveNodeChange(ExerciseNode activeNode, String breadcrumb) {
        if (activeNode == null || activeNode.getId().equals(lastActiveNodeId)) {
            return;
        }

        lastActiveNodeId = activeNode.getId();
        String resourceId = activeNode.getResourceId();
        
        if (resourceId != null && !resourceId.isEmpty()) {
            fetchAndNotifyExercise(activeNode, breadcrumb);
        }
    }

    private static WorkoutExecutionObserver.WorkoutExecutionPhase mapToPhase(PlanNodeState resultState) {
        return switch (resultState) {
            case RUNNING -> WorkoutExecutionObserver.WorkoutExecutionPhase.EXERCISE;
            case WAITING -> WorkoutExecutionObserver.WorkoutExecutionPhase.REST;
            case COMPLETED -> WorkoutExecutionObserver.WorkoutExecutionPhase.COMPLETED;
            // stati transitori (REVERT/SKIPPED/IDLE): la fase corrente resta invariata
            default -> null;
        };
    }

    private void fetchAndNotifyExercise(ExerciseNode activeNode, String breadcrumb) {
        List<ExerciseModifierBean> modifierBeans = activeNode.getResolvedModifiers()
                .stream()
                .map(mod -> new ExerciseModifierBean(
                        IDGenerator.generateUUID(),
                        mod.getType().name(),
                        mod.getValue()
                ))
                .toList();

        CompletableFuture<List<ExerciseDescription>> exerciseDescriptionsFuture = exerciseRepository.getExercisesAsync(List.of(activeNode.getResourceId()));
        CompletableFuture<ExerciseLogBean> lastWeightFuture = logRepository.getLastWeightUsedAsync(activeNode.getResourceId())
                .thenApply(this::exerciseLogEntityToBean);

        exerciseDescriptionsFuture.thenCombine(lastWeightFuture, (exerciseDescriptions, lastWeight) -> {
            if (!exerciseDescriptions.isEmpty()) {
                var entity = exerciseDescriptions.getFirst();
                ExerciseDescriptionBean descriptionBean = new ExerciseDescriptionBean();
                descriptionBean.setExerciseId(entity.getExerciseId());
                descriptionBean.setName(entity.getName());
                descriptionBean.setExecution(entity.getExecution());
                descriptionBean.setMuscleGroups(entity.getMuscleGroups());

                return new CurrentExerciseBean(descriptionBean, modifierBeans, breadcrumb, lastWeight);
            }
            return null;
        }).thenAccept(currentExerciseBean -> {
            if (currentExerciseBean != null) {
                workoutExecutionSubject.notifyCurrentExercise(currentExerciseBean);
            }
        });

        exerciseRepository.getExercisesAsync(List.of(activeNode.getResourceId())).thenAccept(list -> {
            if (!list.isEmpty()) {
                var entity = list.getFirst();
                ExerciseDescriptionBean descriptionBean = new ExerciseDescriptionBean();
                descriptionBean.setExerciseId(entity.getExerciseId());
                descriptionBean.setName(entity.getName());
                descriptionBean.setExecution(entity.getExecution());
                descriptionBean.setMuscleGroups(entity.getMuscleGroups());

                ExerciseLogBean lastWeight = logRepository.getLastWeightUsedAsync(activeNode.getResourceId())
                        .thenApply(this::exerciseLogEntityToBean).join();

                workoutExecutionSubject.notifyCurrentExercise(new CurrentExerciseBean(descriptionBean, modifierBeans, breadcrumb, lastWeight));
            }
        });
    }

    public void play() {
        if (engine != null) {
            engine.play();
            workoutExecutionSubject.notifyCurrentWorkoutEngineState(WorkoutExecutionObserver.WorkoutExecutionState.PLAYING);
        }
    }

    public void pause() {
        if (engine != null) {
            engine.pause();
            workoutExecutionSubject.notifyCurrentWorkoutEngineState(WorkoutExecutionObserver.WorkoutExecutionState.PAUSED);
        }
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

    public ExerciseLogBean getSessionExerciseLog(String exerciseId) {
        return exerciseLogEntityToBean(getOrCreateExerciseLog(exerciseId));
    }

    public void logExerciseSet(String exerciseId, ExerciseSetBean setBean) {
        ExerciseLog log = getOrCreateExerciseLog(exerciseId);
        log.addSets(List.of(new ExerciseSet(setBean.getReps(), setBean.getLoad(), setBean.getRpe())));
    }

    public void updateExerciseNotes(String exerciseId, String notes) {
        getOrCreateExerciseLog(exerciseId).updateNotes(notes);
    }

    private ExerciseLog getOrCreateExerciseLog(String exerciseId) {
        ExerciseLog log = currentSessionLog.getExerciseLog(exerciseId);

        if (log == null) {
            ExerciseDescription exerciseDescription = exerciseRepository.getCachedExercise(exerciseId);
            String exerciseName = exerciseDescription != null ? exerciseDescription.getName() : "Esercizio Sconosciuto";

            log = new ExerciseLog(exerciseName, exerciseId, new ArrayList<>(), "");
            currentSessionLog.addLog(log);
        }

        return log;
    }

    public CompletableFuture<Void> finishAndSaveSession(String sessionNotes) {
        // se il motore non si è già fermato da solo (piano completato), la sessione è interrotta
        String status = "COMPLETED";
        if (engine != null && engine.getState() != null && engine.getState().getStatus() != WorkoutStatus.STOPPED) {
            status = "INTERRUPTED";
            engine.stop();
        }

        SessionLog log = currentSessionLog != null ? currentSessionLog : new SessionLog(System.currentTimeMillis());
        log.updateNotes(sessionNotes);
        log.setStatus(status);
        log.setPlanId(currentPlanId);
        log.setWorkoutSessionDay(currentAbsoluteSessionDay);

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
