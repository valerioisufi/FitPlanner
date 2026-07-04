package com.example.fitplannerclient.repository;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutPlanSummary;
import com.example.fitplannerclient.entity.plan.WorkoutSchedule;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.serializer.PlanDeserializer;
import com.example.fitplannerclient.serializer.PlanToDtoVisitor;
import com.example.fitplannerclient.service.api.WorkoutPlanApi;
import com.example.fitplannercommon.WorkoutScheduleDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WorkoutPlanRepository {

    private final WorkoutPlanApi planApi;
    private final PlanDeserializer deserializer = new PlanDeserializer();

    private WorkoutPlan cachedAssignedPlan;

    public WorkoutPlanRepository(WorkoutPlanApi planApi) {
        this.planApi = planApi;
    }

    public CompletableFuture<WorkoutPlan> createNewPlan() {
        WorkoutPlan plan = new WorkoutPlan("Nuovo piano");
        plan.setCycleLength(7);

        PlanNode rootNode = new Block("Session Giorno 0");
        WorkoutSession firstSession = new WorkoutSession("Sessione 0", 0, rootNode);

        plan.addSession(firstSession);

        PlanToDtoVisitor serializer = new PlanToDtoVisitor();
        plan.accept(serializer);

        return planApi.createPlanAsync(serializer.getPlanDto())
                .thenApply(id -> {
                    plan.setPlanId(id);
                    return plan;
                });
    }

    public CompletableFuture<WorkoutPlan> editExistingPlan(String planId, boolean isCopy) {
        return planApi.getPlanDetailsByIdAsync(planId)
                .thenCompose(planDto -> {
                    WorkoutPlan plan = deserializer.toEntity(planDto);

                    if (isCopy) {
                        plan.setPlanId(null);
                        plan.changeName(plan.getName() + " (Copia)");

                        PlanToDtoVisitor serializer = new PlanToDtoVisitor();
                        plan.accept(serializer);

                        return planApi.createPlanAsync(serializer.getPlanDto())
                                .thenApply(id -> {
                                    plan.setPlanId(id);
                                    return plan;
                                });
                    }

                    return CompletableFuture.completedFuture(plan);
                });
    }

    public CompletableFuture<Void> saveChanges(WorkoutPlan plan) {
        if (plan == null || plan.getPlanId() == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Nessun piano in modifica"));
        }
        PlanToDtoVisitor serializer = new PlanToDtoVisitor();
        plan.accept(serializer);
        return planApi.updatePlanAsync(plan.getPlanId(), serializer.getPlanDto());
    }

    public CompletableFuture<Void> savePlan(WorkoutPlan plan) {
        if (plan == null) return CompletableFuture.completedFuture(null);
        PlanToDtoVisitor serializer = new PlanToDtoVisitor();
        plan.accept(serializer);

        return planApi.updatePlanAsync(plan.getPlanId(), serializer.getPlanDto());
    }

    public CompletableFuture<WorkoutPlan> getAssignedPlanAsync() {
        if (cachedAssignedPlan != null) {
            return CompletableFuture.completedFuture(cachedAssignedPlan);
        }
        return planApi.getAssignedPlanAsync()
                .thenApply(dto -> {
                    if (dto == null) return null;
                    WorkoutPlan plan = deserializer.toEntity(dto);
                    cachedAssignedPlan = plan;
                    return plan;
                });
    }

    public WorkoutPlan getCachedAssignedPlan() {
        return cachedAssignedPlan;
    }

    public CompletableFuture<WorkoutPlan> getPlanByIdAsync(String planId) {
        return planApi.getPlanDetailsByIdAsync(planId)
                .thenApply(dto -> dto == null ? null : deserializer.toEntity(dto));
    }

    public CompletableFuture<List<WorkoutPlanSummary>> getMyCreatedPlansSummaryAsync() {
        return planApi.getMyCreatedPlansSummaryAsync()
                .thenApply(list -> list.stream()
                        .map(dto -> new WorkoutPlanSummary(dto.getPlanId(), dto.getPlanTitle(), dto.getAssignedTo()))
                        .toList());
    }

    public CompletableFuture<WorkoutSchedule> getCurrentCycleScheduleAsync() {
        return planApi.getCurrentCycleScheduleAsync()
                .thenApply(this::scheduleDtoToEntity);
    }

    public CompletableFuture<Void> assignPlanToAthleteAsync(String planId, String athleteId) {
        return planApi.assignPlanToAsync(planId, athleteId);
    }

    public CompletableFuture<Void> deletePlanAsync(String planId) {
        return planApi.deletePlanAsync(planId);
    }


    // mapper DTO -> entity
    private WorkoutSchedule scheduleDtoToEntity(WorkoutScheduleDTO dto) {
        if (dto == null) return null;

        List<WorkoutSchedule.ScheduleDay> days = dto.getDays() == null
                ? List.of()
                : dto.getDays().stream()
                        .map(day -> new WorkoutSchedule.ScheduleDay(
                                day.getAbsoluteDay(),
                                WorkoutSchedule.WorkoutState.valueOf(day.getState().name()))
                        ).toList();

        return new WorkoutSchedule(
                dto.getPlanId(),
                dto.getPlanTitle(),
                dto.getCycleStartDate(),
                dto.getCycleEndDate(),
                dto.getTodayAbsoluteDay(),
                days,
                dto.getSuggestedAbsoluteDay()
        );
    }

}
