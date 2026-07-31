package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.WorkoutPlanDao;
import com.example.fitplannerserver.model.plan.WorkoutPlan;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryWorkoutPlanDao implements WorkoutPlanDao {

    private final Map<String, WorkoutPlan> planById = new ConcurrentHashMap<>();

    private final InMemoryWorkoutSessionDao workoutSessionDao;

    public InMemoryWorkoutPlanDao(InMemoryWorkoutSessionDao workoutSessionDao) {
        this.workoutSessionDao = workoutSessionDao;
    }

    @Override
    public void savePlan(WorkoutPlan plan) {
        Objects.requireNonNull(plan, "WorkoutPlan cannot be null");
        Objects.requireNonNull(plan.getPlanId(), "WorkoutPlan must have a valid planId");

        workoutSessionDao.saveSessionsForPlan(plan.getPlanId(), plan.getAllSessions());
        planById.put(plan.getPlanId(), new WorkoutPlan(plan, false));
    }

    @Override
    public void deletePlan(String planId) {
        Objects.requireNonNull(planId, "planId cannot be null");

        workoutSessionDao.deleteSessionsByPlanId(planId);
        planById.remove(planId);
    }

    @Override
    public Optional<WorkoutPlan> findPlanById(String planId) {
        Objects.requireNonNull(planId, "planId cannot be null");

        WorkoutPlan storedPlan = planById.get(planId);
        if (storedPlan == null) return Optional.empty();

        WorkoutPlan planCopy = new WorkoutPlan(storedPlan);
        workoutSessionDao.findSessionsByPlanId(planId).forEach(planCopy::addSession);

        return Optional.of(planCopy);
    }

    @Override
    public Optional<WorkoutPlan> findAssignedPlanByAthleteId(String athleteId) {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");

        return planById.values().stream()
                .filter(plan -> athleteId.equals(plan.getAssignedToId()))
                .findFirst()
                .map(storedPlan -> {
                    WorkoutPlan copy = new WorkoutPlan(storedPlan);
                    workoutSessionDao.findSessionsByPlanId(copy.getPlanId()).forEach(copy::addSession);
                    return copy;
                });
    }

    @Override
    public List<WorkoutPlan> findPlansByTrainerId(String trainerId) {
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

        return planById.values().stream()
                .filter(plan -> trainerId.equals(plan.getAuthorId()))
                .map(storedPlan -> {
                    WorkoutPlan copy = new WorkoutPlan(storedPlan);
                    workoutSessionDao.findSessionsByPlanId(copy.getPlanId()).forEach(copy::addSession);
                    return copy;
                })
                .toList();
    }

}
