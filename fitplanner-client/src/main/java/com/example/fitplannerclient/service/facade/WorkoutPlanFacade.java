package com.example.fitplannerclient.service.facade;

import com.example.fitplannerclient.service.HttpService;
import com.example.fitplannercommon.WorkoutPlanDTO;
import com.example.fitplannercommon.WorkoutScheduleDTO;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WorkoutPlanFacade {

    private final HttpService httpService;

    public WorkoutPlanFacade(HttpService httpService) {
        this.httpService = httpService;
    }

    /**
     * Recupera tutti i piani di allenamento creati dal trainer.
     */
    public CompletableFuture<List<WorkoutPlanDTO>> getMyCreatedPlansAsync() {
        return httpService.getAsync("/plan", WorkoutPlanDTO[].class)
                .thenApply(Arrays::asList);
    }

    /**
     * Recupera il piano di allenamento attualmente assegnato all'atleta.
     */
    public CompletableFuture<WorkoutPlanDTO> getAssignedPlanAsync() {
        return httpService.getAsync("/plan/assigned", WorkoutPlanDTO.class);
    }

    /**
     * Recupera lo schedule del ciclo corrente dell'atleta.
     */
    public CompletableFuture<WorkoutScheduleDTO> getCurrentCycleScheduleAsync() {
        return httpService.getAsync("/plan/schedule", WorkoutScheduleDTO.class);
    }

    /**
     * Crea un nuovo piano di allenamento vuoto. Ritorna l'ID generato.
     */
    public CompletableFuture<String> createPlanAsync(WorkoutPlanDTO planBean) {
        return httpService.postAsync("/plan", planBean, String.class);
    }

    /**
     * Assegna un piano a un atleta.
     */
    public CompletableFuture<Void> assignPlanToAsync(String planId, String athleteId) {
        return httpService.postAsync("/plan/" + planId + "/assign/" + athleteId, null, Void.class);
    }

    /**
     * Aggiorna un piano di allenamento.
     */
    public CompletableFuture<Void> updatePlanAsync(String planId, WorkoutPlanDTO planBean) {
        return httpService.putAsync("/plan/" + planId, planBean, Void.class);
    }

    /**
     * Elimina un piano di allenamento.
     */
    public CompletableFuture<Void> deletePlanAsync(String planId) {
        return httpService.deleteAsync("/plan/" + planId, Void.class);
    }
}
