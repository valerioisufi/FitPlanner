package com.example.fitplannerclient.controller.plan;

import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutPlanSummary;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.repository.ExerciseRepository;
import com.example.fitplannerclient.repository.WorkoutPlanRepository;
import com.example.fitplannerclient.controller.plan.mapper.PlanToBeanVisitor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class WorkoutPlanManager {

    private final WorkoutPlanRepository planRepository;
    private final ExerciseRepository exerciseRepository;

    public WorkoutPlanManager(WorkoutPlanRepository planRepository, ExerciseRepository exerciseRepository) {
        this.planRepository = planRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public CompletableFuture<List<WorkoutPlanSummaryBean>> getMyCreatedPlansSummaryAsync() {
        return planRepository.getMyCreatedPlansSummaryAsync()
                .thenApply(list -> list
                        .stream()
                        .map(summary -> {
                            WorkoutPlanSummaryBean bean = new WorkoutPlanSummaryBean();
                            bean.setPlanId(summary.planId());
                            bean.setPlanTitle(summary.planTitle());
                            bean.setAssignedTo(summary.assignedTo());
                            return bean;
                        })
                        .toList());
    }

    public CompletableFuture<Void> assignPlanToAthleteAsync(String planId, String athleteEmail) {
        return planRepository.assignPlanToAthleteAsync(planId, athleteEmail);
    }

    public CompletableFuture<WorkoutPlanBean> getAssignedPlanAsync() {
        return planRepository.getAssignedPlanAsync()
                .thenApply(this::entityToBean);
    }

    public CompletableFuture<WorkoutPlanBean> getAssignedPlanOfAthleteAsync(String athleteId) {
        return planRepository.getMyCreatedPlansSummaryAsync()
                .thenCompose(list -> {
                    Optional<String> planId = list.stream()
                            .filter(summary -> summary.assignedTo() != null && summary.assignedTo().equals(athleteId))
                            .findFirst()
                            .map(WorkoutPlanSummary::planId);

                    return planId.map(s -> planRepository.getPlanByIdAsync(s)
                                    .thenApply(this::entityToBean)
                            ).orElseGet(() -> CompletableFuture.completedFuture(null));
                });
    }

    public CompletableFuture<Void> deletePlanAsync(String planId) {
        return planRepository.deletePlanAsync(planId);
    }

    public CompletableFuture<WorkoutScheduleBean> getCurrentCycleScheduleAsync() {
        return planRepository.getCurrentCycleScheduleAsync().thenApply(schedule -> {
            if (schedule == null) return null;
            WorkoutScheduleBean bean = new WorkoutScheduleBean();
            bean.setPlanId(schedule.planId());
            bean.setPlanTitle(schedule.planTitle());
            bean.setCycleStartDate(schedule.cycleStartDate());
            bean.setCycleEndDate(schedule.cycleEndDate());
            bean.setCurrentCycleDay(schedule.currentCycleDay());
            bean.setWorkoutStates(schedule.workoutStates().stream()
                    .map(state -> WorkoutState.valueOf(state.name()))
                    .toList());
            bean.setNextSuggestedSession(mapSessionEntityToBean(schedule.nextSuggestedSession()));
            return bean;
        });
    }

    public CompletableFuture<WorkoutSessionBean> getNextSuggestedSessionAsync() {
        return planRepository.getCurrentCycleScheduleAsync()
                .thenApply(schedule -> {
                    if (schedule == null || schedule.nextSuggestedSession() == null) {
                        return null;
                    }
                    return mapSessionEntityToBean(schedule.nextSuggestedSession());
                });
    }

    // --- MAPPING HELPERS ---

    public WorkoutSessionBean mapSessionEntityToBean(WorkoutSession session) {
        if (session == null) return null;
        PlanNodeBean rootNode = null;
        if (session.getRoot() != null) {
            PlanToBeanVisitor visitor = new PlanToBeanVisitor(this::resolveExerciseName);
            visitor.getAccumulatedDecorators().clear();
            session.getRoot().accept(visitor);
            rootNode = visitor.getCurrentPlanNodeBean();
        }
        if (rootNode == null) {
            rootNode = new PlanNodeBean("root-" + session.getDay(), session.getName(), NodeType.BLOCK);
        }
        return new WorkoutSessionBean(session.getName(), session.getDay(), rootNode);
    }

    public WorkoutPlanBean entityToBean(WorkoutPlan plan) {
        if (plan == null) return null;
        PlanToBeanVisitor visitor = new PlanToBeanVisitor(
                uuid -> {
                    if (exerciseRepository != null) {
                        var exercise = exerciseRepository.getCachedExercise(uuid);
                        if (exercise != null) return exercise.getName();
                    }
                    return "Esercizio Sconosciuto";
                }
        );
        plan.accept(visitor);
        return visitor.getPlanBean();
    }

    private String resolveExerciseName(String uuid) {
        if (exerciseRepository != null) {
            var entity = exerciseRepository.getCachedExercise(uuid);
            if (entity != null) return entity.getName();
        }
        return "Esercizio Sconosciuto";
    }

}
