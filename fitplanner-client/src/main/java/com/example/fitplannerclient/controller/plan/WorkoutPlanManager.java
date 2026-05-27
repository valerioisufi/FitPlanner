package com.example.fitplannerclient.controller.plan;

import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.service.facade.WorkoutPlanFacade;
import com.example.fitplannercommon.*;
import com.example.fitplannerclient.serializer.PlanDeserializer;
import com.example.fitplannerclient.serializer.PlanToBeanVisitor;
import com.example.fitplannerclient.serializer.PlanToDtoVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.JacksonException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class WorkoutPlanManager {

    private final WorkoutPlanFacade planFacade;
    private final ObjectMapper objectMapper;

    public WorkoutPlanManager(WorkoutPlanFacade planFacade) {
        this.planFacade = planFacade;
        this.objectMapper = new ObjectMapper();
    }

//    public CompletableFuture<List<WorkoutPlanBean>> getMyCreatedPlansSummaryAsync() {
//        return planFacade.getMyCreatedPlansSummaryAsync()
//                .thenApply(list -> {
//                    List<WorkoutPlanBean> beans = new ArrayList<>();
//                    for (WorkoutPlanSummaryDTO dto : list) {
//                        beans.add(dtoToBean(dto));
//                    }
//                    return beans;
//                });
//    }

    public CompletableFuture<Void> assignPlanToAthleteAsync(String planId, String athleteEmail) {
        return planFacade.assignPlanToAsync(planId, athleteEmail);
    }

    public CompletableFuture<WorkoutPlanBean> getAssignedPlanAsync() {
        return planFacade.getAssignedPlanAsync()
                .thenApply(this::dtoToBean);
    }

    public CompletableFuture<WorkoutPlanBean> getAssignedPlanOfAthleteAsync(String athleteId) {
        WorkoutPlanBean mockPlan = new WorkoutPlanBean("mock-id", "Piano di Base (Mock)", new ArrayList<>());
        return CompletableFuture.completedFuture(mockPlan);
    }

    public CompletableFuture<WorkoutScheduleDTO> getCurrentCycleScheduleAsync() {
        return planFacade.getCurrentCycleScheduleAsync();
    }

    public CompletableFuture<WorkoutSessionBean> getNextSuggestedSessionAsync() {
        return planFacade.getCurrentCycleScheduleAsync()
                .thenApply(schedule -> {
                    if (schedule == null || schedule.getNextSuggestedSession() == null) {
                        return null;
                    }
                    return mapSessionDtoToBean(schedule.getNextSuggestedSession());
                });
    }

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

    public CompletableFuture<String> createPlanAsync(WorkoutPlanBean planBean) {
        return planFacade.createPlanAsync(beanToDto(planBean));
    }

    public CompletableFuture<Void> assignPlanToAsync(String planId, String athleteId) {
        return planFacade.assignPlanToAsync(planId, athleteId);
    }

    public CompletableFuture<Void> updatePlanAsync(String planId, WorkoutPlanBean planBean) {
        return planFacade.updatePlanAsync(planId, beanToDto(planBean));
    }

    public CompletableFuture<Void> deletePlanAsync(String planId) {
        return planFacade.deletePlanAsync(planId);
    }

    // --- MAPPING HELPERS ---

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

    public WorkoutPlanDTO beanToDto(WorkoutPlanBean bean) {
        if (bean == null) return null;
        try {
            // Reconstruct WorkoutPlan entity tree from bean
            WorkoutPlan plan = new WorkoutPlan(bean.getName(), bean.getId());
            plan.setCycleLength(bean.getCycleLength());

            PlanDeserializer deserializer = new PlanDeserializer();
            if (bean.getSessions() != null) {
                for (WorkoutSessionBean sBean : bean.getSessions()) {
                    PlanNode rootNode = deserializer.toEntity(sBean.getPlanRoot());
                    WorkoutSession session = new WorkoutSession(sBean.getName(), sBean.getDay(), rootNode);
                    plan.addSession(session);
                }
            }

            // Map entity tree to DTO
            PlanToDtoVisitor visitor = new PlanToDtoVisitor();
            plan.accept(visitor);
            return visitor.getPlanDto();
        } catch (Exception e) {
            throw new CompletionException("Failed to map WorkoutPlanBean to WorkoutPlanDTO", e);
        }
    }
}
