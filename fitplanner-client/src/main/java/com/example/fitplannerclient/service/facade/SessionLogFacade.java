package com.example.fitplannerclient.service.facade;

import com.example.fitplannerclient.service.HttpService;
import com.example.fitplannercommon.ExerciseLogDTO;
import com.example.fitplannercommon.SessionLogDTO;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SessionLogFacade {

    private final HttpService httpService;

    public SessionLogFacade(HttpService httpService) {
        this.httpService = httpService;
    }

    /**
     * Salva il log di una sessione di allenamento eseguita.
     */
    public CompletableFuture<Void> saveSessionLogAsync(SessionLogDTO logBean) {
        return httpService.putAsync("/logs/session", logBean, Void.class);
    }

    /**
     * Recupera i log delle sessioni filtrati per data.
     */
    public CompletableFuture<List<SessionLogDTO>> getFilteredSessionLogsAsync(
            String athleteUuid, long startTimestamp, long endTimestamp) {
        
        String url = "/logs/session?startTimestamp=" + startTimestamp + "&endTimestamp=" + endTimestamp;
        if (athleteUuid != null && !athleteUuid.isBlank()) {
            url += "&athleteUuid=" + athleteUuid;
        }
        return httpService.getAsync(url, SessionLogDTO[].class)
                .thenApply(Arrays::asList);
    }

    /**
     * Recupera l'ultimo record per un determinato esercizio.
     */
    public CompletableFuture<ExerciseLogDTO> getLastWeightUsedAsync(String exerciseId) {
        return httpService.getAsync("/logs/exercises/" + exerciseId + "/latest", ExerciseLogDTO.class);
    }
}
