package com.example.fitplannerclient.service.api;

import com.example.fitplannerclient.service.HttpService;
import com.example.fitplannercommon.ExerciseLogDTO;
import com.example.fitplannercommon.SessionLogDTO;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HttpSessionLogApi implements SessionLogApi {

    private final HttpService httpService;

    public HttpSessionLogApi(HttpService httpService) {
        this.httpService = httpService;
    }

    /**
     * Salva il log di una sessione di allenamento eseguita.
     */
    @Override
    public CompletableFuture<Void> saveSessionLogAsync(SessionLogDTO logDTO) {
        return httpService.putAsync("/logs/session", logDTO, Void.class);
    }

    /**
     * Recupera i log delle sessioni filtrati per data.
     */
    @Override
    public CompletableFuture<List<SessionLogDTO>> getFilteredSessionLogsAsync(
            String athleteId, long startTimestamp, long endTimestamp) {
        
        String url = "/logs/session?startTimestamp=" + startTimestamp + "&endTimestamp=" + endTimestamp;
        if (athleteId != null && !athleteId.isBlank()) {
            url += "&athleteId=" + athleteId;
        }
        return httpService.getAsync(url, SessionLogDTO[].class)
                .thenApply(Arrays::asList);
    }

    /**
     * Recupera l'ultimo record per un determinato esercizio.
     */
    @Override
    public CompletableFuture<ExerciseLogDTO> getLastWeightUsedAsync(String exerciseId) {
        return httpService.getAsync("/logs/exercises/" + exerciseId + "/latest", ExerciseLogDTO.class);
    }
}
