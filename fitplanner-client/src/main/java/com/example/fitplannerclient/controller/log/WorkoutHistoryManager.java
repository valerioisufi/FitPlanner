package com.example.fitplannerclient.controller.log;

import com.example.fitplannerclient.service.api.SessionLogApi;
import com.example.fitplannercommon.SessionLogDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WorkoutHistoryManager {
    private final SessionLogApi logApi;

    public WorkoutHistoryManager(SessionLogApi logApi) {
        this.logApi = logApi;
    }

    public CompletableFuture<List<SessionLogDTO>> getFilteredSessionLogsAsync(
            String athleteUuid, long startTimestamp, long endTimestamp) {
        return logApi.getFilteredSessionLogsAsync(athleteUuid, startTimestamp, endTimestamp);
    }
}
