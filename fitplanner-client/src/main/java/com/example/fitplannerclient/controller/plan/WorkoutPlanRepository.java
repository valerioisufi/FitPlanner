package com.example.fitplannerclient.controller.plan;

import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.entity.ExerciseDescription;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.repository.ExerciseRepository;
import com.example.fitplannerclient.serializer.PlanDeserializer;
import com.example.fitplannerclient.serializer.PlanToBeanVisitor;
import com.example.fitplannerclient.serializer.PlanToDtoVisitor;
import com.example.fitplannerclient.service.api.WorkoutPlanApi;

import java.util.concurrent.CompletableFuture;

public class WorkoutPlanRepository {

    private final WorkoutPlanApi planApi;
    private final ExerciseRepository exerciseRepository;

    public WorkoutPlanRepository(WorkoutPlanApi planApi, ExerciseRepository exerciseRepository) {
        this.planApi = planApi;
        this.exerciseRepository = exerciseRepository;
        
        if (exerciseRepository != null) {
            exerciseRepository.getExercisesAsync(null).thenAccept(entities -> {});
        }
    }

    public CompletableFuture<WorkoutPlanBean> getPlanAsync(WorkoutPlan plan) {
        if (plan == null) return CompletableFuture.completedFuture(null);

        if (exerciseRepository == null) {
            PlanToBeanVisitor visitor = new PlanToBeanVisitor();
            plan.accept(visitor);
            return CompletableFuture.completedFuture(visitor.getPlanBean());
        }

        return exerciseRepository.getExercisesAsync(null).thenApply(entities -> {
            PlanToBeanVisitor visitor = new PlanToBeanVisitor(
                    uuid -> {
                        ExerciseDescription entity = exerciseRepository.getCachedExercise(uuid);
                        return entity != null ? entity.getName() : "Esercizio Sconosciuto";
                    }
            );
            plan.accept(visitor);
            return visitor.getPlanBean();
        });
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
        PlanDeserializer deserializer = new PlanDeserializer();

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
}
