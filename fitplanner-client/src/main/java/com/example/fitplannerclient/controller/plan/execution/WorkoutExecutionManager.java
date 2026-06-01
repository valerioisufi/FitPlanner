package com.example.fitplannerclient.controller.plan.execution;

import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.controller.plan.WorkoutPlanRepository;
import com.example.fitplannerclient.controller.plan.execution.engine.WorkoutEngine;
import com.example.fitplannerclient.controller.plan.execution.engine.WorkoutEngineImpl;
import com.example.fitplannerclient.controller.plan.execution.observer.WorkoutExecutionObserver;
import com.example.fitplannerclient.controller.plan.execution.observer.WorkoutExecutionSubject;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.context.WorkoutStatus;
import com.example.fitplannerclient.repository.ExerciseRepository;
import com.example.fitplannerclient.serializer.PlanToBeanVisitor;
import com.example.fitplannerclient.service.api.SessionLogApi;
import com.example.fitplannercommon.ExerciseLogDTO;
import com.example.fitplannercommon.SessionLogDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WorkoutExecutionManager {

    private final SessionLogApi logApi;
    private final WorkoutPlanRepository planRepository;
    private final ExerciseRepository exerciseRepository;

    private WorkoutEngine engine;

    private String currentPlanId;
    private WorkoutSession currentSession;

    private WorkoutPlan currentPlan;

    private WorkoutExecutionSubject workoutExecutionSubject = new WorkoutExecutionSubject();
    private String lastActiveNodeId = null;

    public WorkoutExecutionManager(WorkoutPlanRepository planRepository, SessionLogApi logApi, ExerciseRepository exerciseRepository) {
        this.planRepository = planRepository;
        this.logApi = logApi;
        this.exerciseRepository = exerciseRepository;
    }

    public void attachObserver(WorkoutExecutionObserver observer) {
        workoutExecutionSubject.attach(observer);
    }

    public void detachObserver(WorkoutExecutionObserver observer) {
        workoutExecutionSubject.detach(observer);
    }

    public CompletableFuture<Void> startSessionAsync(String planId, int workoutSessionDay) {
        this.currentPlanId = planId;

        // Recupera il piano direttamente tramite il repository (sfrutta la cache se possibile)
        return planRepository.getAssignedPlanAsync()
                .thenAccept(plan -> {
                    if (plan == null) throw new IllegalStateException("Nessun piano assegnato");
                    this.currentPlan = plan;

                    WorkoutSession targetSession = null;
                    for (WorkoutSession s : plan.getSessions()) {
                        if (s.getDay() == workoutSessionDay) {
                            targetSession = s;
                            break;
                        }
                    }
                    if (targetSession == null) {
                        throw new IllegalArgumentException("Sessione non trovata per il giorno specificato");
                    }
                    this.currentSession = targetSession;

                    // Inizializza il motore passando la radice della sessione (Entity)
                    this.engine = new WorkoutEngineImpl(this.currentSession.getRoot());

                    // Inizializza la callback funzionale (Clean Architecture)
                    this.engine.setOnUpdateListener((state, activeNode, timeRemaining) -> {
                        WorkoutStatus status = (state != null) ? state.getStatus() : WorkoutStatus.UNKNOWN;
                        
                        WorkoutExecutionObserver.WorkoutExecutionState observerState = WorkoutExecutionObserver.WorkoutExecutionState.STOPPED;
                        if (status == WorkoutStatus.PLAYING) observerState = WorkoutExecutionObserver.WorkoutExecutionState.PLAYING;
                        else if (status == WorkoutStatus.PAUSED) observerState = WorkoutExecutionObserver.WorkoutExecutionState.PAUSED;
                        
                        workoutExecutionSubject.notifyCurrentWorkoutEngineState(observerState);
                        
                        if (timeRemaining > 0) {
                            workoutExecutionSubject.notifyCurrentRestTime(timeRemaining / 1000);
                        } else if (timeRemaining <= 0) {
                            // Non siamo in pausa/riposo, annulliamo il timer visivamente o notifichiamo un tempo 0?
                            // Il controller gestirà la logica nascondendo il timer quando riceve un updateCurrentExercise.
                        }
                        
                        if (activeNode != null && !activeNode.getId().equals(lastActiveNodeId)) {
                            lastActiveNodeId = activeNode.getId();
                            String resourceId = activeNode.getResourceId();
                            if (resourceId != null && !resourceId.isEmpty()) {
                                exerciseRepository.getExercisesAsync(List.of(resourceId)).thenAccept(list -> {
                                    if (!list.isEmpty()) {
                                        var entity = list.get(0);
                                        com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean bean = new com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean();
                                        bean.setExerciseId(entity.getExerciseId());
                                        bean.setName(entity.getName());
                                        bean.setExecution(entity.getExecution());
                                        bean.setMuscleGroups(entity.getMuscleGroups());
                                        workoutExecutionSubject.notifyCurrentExercise(bean);
                                    }
                                });
                            }
                        }
                    });
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

    public CompletableFuture<ExerciseLogDTO> getLastWeightUsedAsync(String exerciseId) {
        return logApi.getLastWeightUsedAsync(exerciseId);
    }

    public CompletableFuture<Void> finishAndSaveSession() {
        if (engine != null) {
            engine.stop();
        }

        SessionLogDTO.SessionStatus status = SessionLogDTO.SessionStatus.COMPLETED;
        if (engine != null && engine.getState() != null && engine.getState().getStatus() != WorkoutStatus.STOPPED) {
            status = SessionLogDTO.SessionStatus.INTERRUPTED;
        }

        SessionLogDTO logDTO = new SessionLogDTO(
                "current-user-id", // TODO: recuperare da AuthManager/ProfileManager
                "Sessione completata tramite App",
                status,
                System.currentTimeMillis(),
                currentPlanId,
                currentSession != null ? currentSession.getDay() : 0,
                new ArrayList<>() // TODO: mappare i risultati dall'ExecutionContext
        );

        return logApi.saveSessionLogAsync(logDTO);
    }
}
