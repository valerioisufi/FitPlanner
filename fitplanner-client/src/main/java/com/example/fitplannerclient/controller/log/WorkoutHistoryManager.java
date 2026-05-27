package com.example.fitplannerclient.controller.log;

import com.example.fitplannerclient.service.facade.SessionLogFacade;
import com.example.fitplannercommon.SessionLogDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WorkoutHistoryManager {
    private final SessionLogFacade logFacade;

    public WorkoutHistoryManager(SessionLogFacade logFacade) {
        this.logFacade = logFacade;
    }

    public CompletableFuture<List<SessionLogDTO>> getFilteredSessionLogsAsync(
            String athleteUuid, long startTimestamp, long endTimestamp) {
        return logFacade.getFilteredSessionLogsAsync(athleteUuid, startTimestamp, endTimestamp);
    }
}
