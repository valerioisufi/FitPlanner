package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.WorkoutPlanDao;
import com.example.fitplannerserver.model.plan.WorkoutPlan;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryWorkoutPlanDao implements WorkoutPlanDao{

    private final Map<String, WorkoutPlan> planById = new ConcurrentHashMap<>();

    @Override
    public void savePlan(WorkoutPlan plan) {
        Objects.requireNonNull(plan, "WorkoutPlan cannot be null");
        Objects.requireNonNull(plan.getPlanId(), "WorkoutPlan must have a valid planId");

        planById.put(plan.getPlanId(), new WorkoutPlan(plan));
    }

    @Override
    public void deletePlan(String planId) {
        Objects.requireNonNull(planId, "planId cannot be null");

        planById.remove(planId);
    }

    @Override
    public Optional<WorkoutPlan> findPlanById(String planId) {
        Objects.requireNonNull(planId, "planId cannot be null");

        return Optional.ofNullable(planById.get(planId)).map(WorkoutPlan::new);
    }

    @Override
    public Optional<WorkoutPlan> findAssignedPlanByAthleteId(String athleteId) {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");

        return planById.values().stream()
                .filter(plan -> athleteId.equals(plan.getAssignedToId()))
                .findFirst()
                .map(WorkoutPlan::new);
    }

    @Override
    public List<WorkoutPlan> findPlansByTrainerId(String trainerId) {
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

        return planById.values().stream()
                .filter(plan -> trainerId.equals(plan.getAuthorId()))
                .map(WorkoutPlan::new)
                .toList();
    }

}
