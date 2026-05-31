package com.example.fitplannerclient.controller.plan.execution;

import com.example.fitplannerclient.controller.plan.execution.engine.WorkoutEngine;
import com.example.fitplannerclient.controller.plan.execution.engine.WorkoutEngineImpl;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.context.WorkoutStatus;
import com.example.fitplannerclient.service.api.SessionLogApi;
import com.example.fitplannercommon.ExerciseLogDTO;
import com.example.fitplannercommon.SessionLogDTO;
import com.example.fitplannerclient.bean.plan.WorkoutSessionBean;
import com.example.fitplannerclient.serializer.PlanDeserializer;
import com.example.fitplannerclient.entity.plan.PlanNode;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class WorkoutExecutionManager {

    private WorkoutEngine engine;
    private final SessionLogApi logApi;

    private String currentPlanId;
    private WorkoutSession currentSession;

    // TODO: Aggiungi qui il subject per la UI
    // private WorkoutExecutionSubject uiSubject = new WorkoutExecutionSubject();

    public WorkoutExecutionManager(SessionLogApi logApi) {
        this.logApi = logApi;
    }

    public void startSession(String planId, WorkoutSessionBean sessionBean) {
        this.currentPlanId = planId;

        // Deserializza il Bean della sessione in Entity di dominio
        PlanDeserializer deserializer = new PlanDeserializer();
        PlanNode rootNode = deserializer.toEntity(sessionBean.getPlanRoot());
        this.currentSession = new WorkoutSession(sessionBean.getName(), sessionBean.getDay(), rootNode);

        // Inizializza il motore passando la radice della sessione
        this.engine = new WorkoutEngineImpl(this.currentSession.getRoot());

        // Inizializza la callback funzionale (Clean Architecture)
        this.engine.setOnUpdateListener((state, activeNode, timeRemaining) -> {
            
            // 1. Leggi lo stato (usando l'Enum condiviso)
            WorkoutStatus status = (state != null) ? state.getStatus() : WorkoutStatus.UNKNOWN;

            // 2. Ottieni l'ID del nodo (DTO)
            String activeNodeId = (activeNode != null) ? activeNode.getId() : null;

            // 3. Notifica la UI (passando l'Enum)
            // uiSubject.notifyObservers(status, activeNodeId, timeRemaining);
            
            System.out.println("UI Notificata -> Stato: " + status + ", Nodo: " + activeNodeId + ", Tempo: " + timeRemaining);
        });
    }

    public void play() {
        engine.play();
    }

    public void pause() {
        engine.pause();
    }

    public void stop() {
        engine.stop();
    }

    public void skipNext() {
        engine.skipNext();
    }

    public void skipPrevious() {
        engine.skipPrevious();
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
        if (engine != null && engine.getState().getStatus() != WorkoutStatus.STOPPED) {
            // Se l'engine non era fermo ma l'utente ha terminato forzatamente
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
