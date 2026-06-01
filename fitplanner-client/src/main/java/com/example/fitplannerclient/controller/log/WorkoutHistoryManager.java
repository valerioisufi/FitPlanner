package com.example.fitplannerclient.controller.log;

import com.example.fitplannerclient.bean.log.SessionLogBean;
import com.example.fitplannerclient.entity.log.SessionLog;
import com.example.fitplannerclient.repository.SessionLogRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WorkoutHistoryManager {
    private final SessionLogRepository logRepository;

    public WorkoutHistoryManager(SessionLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public CompletableFuture<List<SessionLogBean>> getFilteredSessionLogsAsync(
            String athleteId, long startTimestamp, long endTimestamp) {
        return logRepository.getFilteredSessionLogsAsync(athleteId, startTimestamp, endTimestamp)
                .thenApply(entities -> entities.stream()
                        .map(this::entityToBean)
                        .collect(Collectors.toList()));
    }

    private SessionLogBean entityToBean(SessionLog entity) {
        return new SessionLogBean(
                entity.getUserId(),
                entity.getWorkoutSessionDay(),
                entity.getDate(),
                entity.getStatus(),
                entity.getPlanId()
        );
    }
}
