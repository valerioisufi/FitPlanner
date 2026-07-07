package com.example.fitplannerclient.controller.plan;

import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.entity.ExerciseDescription;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutPlanSummary;
import com.example.fitplannerclient.entity.plan.WorkoutSchedule;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.repository.ExerciseRepository;
import com.example.fitplannerclient.repository.WorkoutPlanRepository;
import com.example.fitplannerclient.controller.plan.mapper.PlanToBeanVisitor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
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
        CompletableFuture<WorkoutPlan> planFuture = planRepository.getAssignedPlanAsync();
        CompletableFuture<List<ExerciseDescription>> exercisesFuture = exerciseRepository.getExercisesAsync(null);

        return CompletableFuture.allOf(planFuture, exercisesFuture)
                .thenApply(v -> entityToBean(planFuture.join()));
    }

    public CompletableFuture<WorkoutPlanBean> getAssignedPlanOfAthleteAsync(String athleteId) {
        return planRepository.getMyCreatedPlansSummaryAsync()
                .thenCompose(list -> {
                    Optional<String> planId = list.stream()
                            .filter(summary -> summary.assignedTo() != null && summary.assignedTo().equals(athleteId))
                            .findFirst()
                            .map(WorkoutPlanSummary::planId);

                    return planId.map(s -> {
                        CompletableFuture<WorkoutPlan> planFuture = planRepository.getPlanByIdAsync(s);
                        CompletableFuture<List<ExerciseDescription>> exercisesFuture = exerciseRepository.getExercisesAsync(null);
                        
                        return CompletableFuture.allOf(planFuture, exercisesFuture)
                                .thenApply(v -> entityToBean(planFuture.join()));
                    }).orElseGet(() -> CompletableFuture.completedFuture(null));
                });
    }

    public CompletableFuture<Void> deletePlanAsync(String planId) {
        return planRepository.deletePlanAsync(planId);
    }

    /**
     * Calendario del ciclo corrente: ogni giorno
     * porta data, stato, flag "oggi" e la sessione del piano che vi cade.
     * Restituisce null se non c'è un piano assegnato
     */
    public CompletableFuture<WorkoutScheduleBean> getCurrentCycleScheduleAsync() {
        CompletableFuture<WorkoutSchedule> scheduleFuture = planRepository.getCurrentCycleScheduleAsync();
        CompletableFuture<WorkoutPlan> planFuture = planRepository.getAssignedPlanAsync();
        CompletableFuture<List<ExerciseDescription>> exercisesFuture = exerciseRepository.getExercisesAsync(null);

        return CompletableFuture.allOf(scheduleFuture, planFuture, exercisesFuture)
                .thenApply(v -> buildScheduleBean(scheduleFuture.join(), planFuture.join()));
    }

    private WorkoutScheduleBean buildScheduleBean(WorkoutSchedule schedule, WorkoutPlan plan) {
        if (schedule == null || plan == null) return null;

        WorkoutScheduleBean bean = new WorkoutScheduleBean();
        bean.setPlanId(schedule.planId());
        bean.setPlanTitle(schedule.planTitle());
        bean.setCycleStartDate(schedule.cycleStartDate());
        bean.setCycleEndDate(schedule.cycleEndDate());

        LocalDate cycleStart = LocalDate.ofInstant(Instant.ofEpochMilli(schedule.cycleStartDate()), ZoneOffset.UTC);

        List<ScheduleDayBean> days = new ArrayList<>();
        int suggestedDayIndex = -1;

        for (int i = 0; i < schedule.days().size(); i++) {
            WorkoutSchedule.ScheduleDay day = schedule.days().get(i);

            ScheduleDayBean dayBean = new ScheduleDayBean();
            dayBean.setAbsoluteDay(day.absoluteDay());
            dayBean.setDate(cycleStart.plusDays(i).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
            dayBean.setState(WorkoutState.valueOf(day.state().name()));
            dayBean.setToday(day.absoluteDay() == schedule.todayAbsoluteDay());

            if (day.state() != WorkoutSchedule.WorkoutState.REST) {
                // il contenuto della sessione viene recuperato dal piano
                int cycleDay = day.absoluteDay() % plan.getCycleLength();
                dayBean.setSession(mapSessionEntityToBean(plan.getSessionByDay(cycleDay)));
            }

            if (day.absoluteDay() == schedule.suggestedAbsoluteDay() && dayBean.getSession() != null) {
                suggestedDayIndex = i;
            }

            days.add(dayBean);
        }

        bean.setDays(days);
        bean.setSuggestedDayIndex(suggestedDayIndex);
        return bean;
    }

    // --- MAPPING HELPERS ---

    public WorkoutSessionBean mapSessionEntityToBean(WorkoutSession session) {
        if (session == null) return null;
        PlanNodeBean rootNode = null;
        if (session.getRoot() != null) {
            PlanToBeanVisitor visitor = new PlanToBeanVisitor(this::resolveExerciseName);
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
        PlanToBeanVisitor visitor = new PlanToBeanVisitor(this::resolveExerciseName);
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
