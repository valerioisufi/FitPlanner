package com.example.fitplannerclient.service.api;

import com.example.fitplannercommon.WorkoutPlanDTO;
import com.example.fitplannercommon.WorkoutPlanSummaryDTO;
import com.example.fitplannercommon.WorkoutScheduleDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface WorkoutPlanApi {
    CompletableFuture<List<WorkoutPlanSummaryDTO>> getMyCreatedPlansSummaryAsync();
    CompletableFuture<WorkoutPlanDTO> getPlanDetailsByIdAsync(String planId);
    CompletableFuture<WorkoutPlanDTO> getAssignedPlanAsync();
    CompletableFuture<WorkoutScheduleDTO> getCurrentCycleScheduleAsync();
    CompletableFuture<String> createPlanAsync(WorkoutPlanDTO planBean);
    CompletableFuture<Void> assignPlanToAsync(String planId, String athleteId);
    CompletableFuture<Void> updatePlanAsync(String planId, WorkoutPlanDTO planBean);
    CompletableFuture<Void> deletePlanAsync(String planId);
}
