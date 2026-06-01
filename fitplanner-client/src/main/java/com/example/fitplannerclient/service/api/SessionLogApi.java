package com.example.fitplannerclient.service.api;

import com.example.fitplannercommon.ExerciseLogDTO;
import com.example.fitplannercommon.SessionLogDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SessionLogApi {
    CompletableFuture<Void> saveSessionLogAsync(SessionLogDTO logDTO);
    CompletableFuture<List<SessionLogDTO>> getFilteredSessionLogsAsync(String athleteUuid, long startTimestamp, long endTimestamp);
    CompletableFuture<ExerciseLogDTO> getLastWeightUsedAsync(String exerciseId);
}
