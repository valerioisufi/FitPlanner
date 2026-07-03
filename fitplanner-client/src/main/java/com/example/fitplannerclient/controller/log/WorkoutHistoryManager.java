package com.example.fitplannerclient.controller.log;

import com.example.fitplannerclient.bean.log.FilterBean;
import com.example.fitplannerclient.bean.log.SessionLogBean;
import com.example.fitplannerclient.bean.log.StatisticsBean;
import com.example.fitplannerclient.entity.log.ExerciseLog;
import com.example.fitplannerclient.entity.log.SessionLog;
import com.example.fitplannerclient.repository.SessionLogRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WorkoutHistoryManager {
    private final SessionLogRepository logRepository;
    private final String athleteId;

    public WorkoutHistoryManager(SessionLogRepository logRepository, String athleteId) {
        this.logRepository = logRepository;
        this.athleteId = athleteId;
    }

    public CompletableFuture<List<SessionLogBean>> getFilteredSessionLogsAsync(
            long startTimestamp, long endTimestamp
    ) {
        return logRepository.getFilteredSessionLogsAsync(this.athleteId, startTimestamp, endTimestamp)
                .thenApply(entities -> entities.stream()
                        .map(this::entityToBean)
                        .toList()
                );
    }

    public CompletableFuture<List<StatisticsBean>> getStatisticsAsync(FilterBean filterBean) {
        return logRepository.getFilteredSessionLogsAsync(this.athleteId, filterBean.startDate(), filterBean.endDate())
                .thenApply(sessionLogs -> {
                    List<StatisticsBean> statisticsList = new ArrayList<>();

                    for (String exerciseId : filterBean.exercises().keySet()) {
                        statisticsList.add(new StatisticsBean(
                                filterBean.startDate(),
                                filterBean.endDate(),
                                exerciseId,
                                filterBean.exercises().get(exerciseId),
                                getExercisesStatisticsLog(sessionLogs, exerciseId)
                        ));
                    }

                    return statisticsList;
                });
    }

    public CompletableFuture<FilterBean> getFiltersAsync(long startDate, long endDate) {
        return logRepository.getFilteredSessionLogsAsync(this.athleteId, startDate, endDate)
                .thenApply(entities -> {
                    Map<String, String> exercises = new HashMap<>();

                    for (SessionLog entity : entities) {
                        entity.getExerciseLogs().forEach(exerciseLog ->
                            exercises.computeIfAbsent(exerciseLog.getExerciseId(), k -> exerciseLog.getName())
                        );
                    }
                    return new FilterBean(startDate, endDate, exercises);
                });
    }

    private List<StatisticsBean.SessionStatisticsBean> getExercisesStatisticsLog(List<SessionLog> sessionLogs, String exerciseId) {
        List<StatisticsBean.SessionStatisticsBean> sessionStatisticsList = new ArrayList<>();

        for (SessionLog sessionLog : sessionLogs) {
            ExerciseLog exerciseLog = sessionLog.getExerciseLog(exerciseId);
            if (exerciseLog != null) {
                sessionStatisticsList.add(new StatisticsBean.SessionStatisticsBean(
                        sessionLog.getDate(), exerciseLog.calculateTotalVolume()));
            }
        }
        return sessionStatisticsList;
    }

    // mapper entity -> bean

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
