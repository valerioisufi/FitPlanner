package com.example.fitplannerclient.repository;

import com.example.fitplannerclient.entity.log.SessionLog;
import com.example.fitplannerclient.service.api.SessionLogApi;
import com.example.fitplannercommon.SessionLogDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SessionLogRepository {

    private final SessionLogApi logApi;
    
    // Cache map: athleteId -> CachedData
    private final Map<String, CachedLogs> cache = new HashMap<>();

    public SessionLogRepository(SessionLogApi logApi) {
        this.logApi = logApi;
    }

    public CompletableFuture<List<SessionLog>> getFilteredSessionLogsAsync(String athleteId, long startTimestamp, long endTimestamp) {
        CachedLogs athleteCache = cache.computeIfAbsent(athleteId, k -> new CachedLogs());

        if (athleteCache.isFullyCovered(startTimestamp, endTimestamp)) {
            return CompletableFuture.completedFuture(athleteCache.getLogsInRange(startTimestamp, endTimestamp));
        }

        // Se non coperto completamente, facciamo una chiamata per l'intero range.
        // Possiamo estendere il min/max del range richiesto con il min/max del range attualmente in cache.
        long queryStart = Math.min(startTimestamp, athleteCache.minTimestamp);
        long queryEnd = Math.max(endTimestamp, athleteCache.maxTimestamp);

        return logApi.getFilteredSessionLogsAsync(athleteId, queryStart, queryEnd)
                .thenApply(dtoList -> {
                    List<SessionLog> entities = new ArrayList<>();
                    if (dtoList != null) {
                        for (SessionLogDTO dto : dtoList) {
                            SessionLog entity = mapToEntity(dto);
                            entities.add(entity);
                        }
                    }
                    // Sostituiamo la cache per questo range (o unifichiamo)
                    athleteCache.updateCache(queryStart, queryEnd, entities);
                    return athleteCache.getLogsInRange(startTimestamp, endTimestamp);
                });
    }

    private SessionLog mapToEntity(SessionLogDTO dto) {
        SessionLog entity = new SessionLog(dto.getDate());
        entity.setUserId(dto.getUserId());
        entity.updateNotes(dto.getNotes());
        entity.setWorkoutSessionDay(dto.getWorkoutSessionDay());
        entity.setPlanId(dto.getPlanIdReference());
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus().name());
        }
        return entity;
    }

    private static class CachedLogs {
        long minTimestamp = Long.MAX_VALUE;
        long maxTimestamp = Long.MIN_VALUE;
        final List<SessionLog> logs = new ArrayList<>();

        boolean isFullyCovered(long start, long end) {
            if (minTimestamp == Long.MAX_VALUE || maxTimestamp == Long.MIN_VALUE) return false;
            return start >= minTimestamp && end <= maxTimestamp;
        }

        void updateCache(long start, long end, List<SessionLog> newLogs) {
            this.minTimestamp = start;
            this.maxTimestamp = end;
            this.logs.clear();
            this.logs.addAll(newLogs);
        }

        List<SessionLog> getLogsInRange(long start, long end) {
            List<SessionLog> result = new ArrayList<>();
            for (SessionLog log : logs) {
                if (log.getDate() >= start && log.getDate() <= end) {
                    result.add(log);
                }
            }
            return result;
        }
    }
}
