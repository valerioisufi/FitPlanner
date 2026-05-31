package com.example.fitplannerclient.controller.plan;

import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.service.api.WorkoutPlanApi;
import com.example.fitplannercommon.*;
import com.example.fitplannerclient.serializer.PlanDeserializer;
import com.example.fitplannerclient.serializer.PlanToBeanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class WorkoutPlanManager {

    private final WorkoutPlanApi planApi;

    public WorkoutPlanManager(WorkoutPlanApi planApi) {
        this.planApi = planApi;
    }

    public CompletableFuture<List<WorkoutPlanSummaryBean>> getMyCreatedPlansSummaryAsync() {
        return planApi.getMyCreatedPlansSummaryAsync()
                .thenApply(list -> list
                        .stream()
                        .map(dto -> {
                            WorkoutPlanSummaryBean bean = new WorkoutPlanSummaryBean();
                            bean.setPlanId(dto.getPlanId());
                            bean.setPlanTitle(dto.getPlanTitle());
                            bean.setAssignedTo(dto.getAssignedTo());
                            return bean;
                        })
                        .toList());
    }

    public CompletableFuture<Void> assignPlanToAthleteAsync(String planId, String athleteEmail) {
        return planApi.assignPlanToAsync(planId, athleteEmail);
    }

    public CompletableFuture<WorkoutPlanBean> getAssignedPlanAsync() {
        return planApi.getAssignedPlanAsync()
                .thenApply(this::dtoToBean);
    }

    public CompletableFuture<WorkoutPlanBean> getAssignedPlanOfAthleteAsync(String athleteId) {
        return planApi.getMyCreatedPlansSummaryAsync()
                .thenCompose(list -> {
                    Optional<String> planId = list.stream()
                            .filter(dto -> dto.getAssignedTo() != null && dto.getAssignedTo().equals(athleteId))
                            .findFirst()
                            .map(WorkoutPlanSummaryDTO::getPlanId);

                    return planId.map(s -> planApi.getPlanDetailsByIdAsync(s)
                                    .thenApply(this::dtoToBean)
                            ).orElseGet(() -> CompletableFuture.completedFuture(null));

                });
    }

    public CompletableFuture<Void> deletePlanAsync(String planId) {
        return planApi.deletePlanAsync(planId);
    }

    public CompletableFuture<WorkoutScheduleBean> getCurrentCycleScheduleAsync() {
        return planApi.getCurrentCycleScheduleAsync().thenApply(dto -> {
            if (dto == null) return null;
            WorkoutScheduleBean bean = new WorkoutScheduleBean();
            bean.setPlanId(dto.getPlanId());
            bean.setPlanTitle(dto.getPlanTitle());
            bean.setCycleStartDate(dto.getCycleStartDate());
            bean.setCycleEndDate(dto.getCycleEndDate());
            bean.setCurrentCycleDay(dto.getCurrentCycleDay());
            bean.setWorkoutStates(dto.getWorkoutStates());
            bean.setNextSuggestedSession(mapSessionDtoToBean(dto.getNextSuggestedSession()));
            return bean;
        });
    }

    public CompletableFuture<WorkoutSessionBean> getNextSuggestedSessionAsync() {
        return planApi.getCurrentCycleScheduleAsync()
                .thenApply(schedule -> {
                    if (schedule == null || schedule.getNextSuggestedSession() == null) {
                        return null;
                    }
                    return mapSessionDtoToBean(schedule.getNextSuggestedSession());
                });
    }

    // --- MAPPING HELPERS ---

    public WorkoutSessionBean mapSessionDtoToBean(WorkoutSessionDTO sDto) {
        if (sDto == null) return null;
        PlanNodeBean rootNode = null;
        if (sDto.getContent() != null && !sDto.getContent().isBlank()) {
            try {
                PlanDeserializer deserializer = new PlanDeserializer();
                PlanNode root = deserializer.deserialize(sDto.getContent());
                if (root != null) {
                    PlanToBeanVisitor visitor = new PlanToBeanVisitor();
                    visitor.getAccumulatedDecorators().clear();
                    root.accept(visitor);
                    rootNode = visitor.getCurrentPlanNodeBean();
                }
            } catch (Exception e) {
                throw new CompletionException("Failed to deserialize workout session content", e);
            }
        }
        if (rootNode == null) {
            rootNode = new PlanNodeBean("root-" + sDto.getDay(), sDto.getName(), NodeType.BLOCK);
        }
        return new WorkoutSessionBean(String.valueOf(sDto.getDay()), sDto.getDay(), rootNode);
    }

    public WorkoutPlanBean dtoToBean(WorkoutPlanDTO dto) {
        if (dto == null) return null;
        try {
            PlanDeserializer deserializer = new PlanDeserializer();
            WorkoutPlan entity = deserializer.toEntity(dto);

            PlanToBeanVisitor visitor = new PlanToBeanVisitor();
            entity.accept(visitor);

            return visitor.getPlanBean();
        } catch (Exception e) {
            throw new CompletionException("Failed to map WorkoutPlanDTO to WorkoutPlanBean", e);
        }
    }

}
