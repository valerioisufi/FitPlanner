package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.WorkoutPlanDao;
import com.example.fitplannerserver.model.plan.WorkoutPlan;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryWorkoutPlanDao implements WorkoutPlanDao{

    private static class Wrapper {
        public static final InMemoryWorkoutPlanDao INSTANCE = new InMemoryWorkoutPlanDao();
    }

    private InMemoryWorkoutPlanDao(){}

    public static InMemoryWorkoutPlanDao getInstance() {
        return Wrapper.INSTANCE;
    }

    private final Map<String, WorkoutPlan> planById = new ConcurrentHashMap<>();
    private final Map<String, List<String>> plansIdByTrainerId = new ConcurrentHashMap<>();
    private final Map<String, String> planIdByAthleteId = new ConcurrentHashMap<>();

    @Override
    public synchronized void savePlan(WorkoutPlan plan) {
        Objects.requireNonNull(plan, "WorkoutPlan cannot be null");
        Objects.requireNonNull(plan.getPlanId(), "WorkoutPlan must have a valid planId");
        Objects.requireNonNull(plan.getAuthorId(), "WorkoutPlan must have a valid authorTrainerId");

        WorkoutPlan copyOfWorkoutPlan = new WorkoutPlan(plan);

        WorkoutPlan existingPlan = planById.get(copyOfWorkoutPlan.getPlanId());
        if (existingPlan != null && existingPlan.getAssignedToId() != null) {
            // il piano precedente era assegnato già a qualcun altro
            if (!existingPlan.getAssignedToId().equals(copyOfWorkoutPlan.getAssignedToId())) {
                planIdByAthleteId.remove(existingPlan.getAssignedToId());
            }
        }


        planById.put(copyOfWorkoutPlan.getPlanId(), copyOfWorkoutPlan);

        String athleteId = copyOfWorkoutPlan.getAssignedToId();
        if(athleteId != null) assignPlanToAthlete(copyOfWorkoutPlan.getPlanId(), athleteId);

        String trainerId = copyOfWorkoutPlan.getAuthorId();
        List<String> plansId = plansIdByTrainerId.computeIfAbsent(
                trainerId,
                k -> new CopyOnWriteArrayList<>());

        if(!plansId.contains(copyOfWorkoutPlan.getPlanId())) {
            plansId.add(copyOfWorkoutPlan.getPlanId());
        }

    }

    @Override
    public synchronized void deletePlan(String planId) {
        Objects.requireNonNull(planId, "planId cannot be null");

        WorkoutPlan planToDelete = planById.get(planId);
        if(planToDelete != null) {
            String athleteId = planToDelete.getAssignedToId();
            if(athleteId != null) planIdByAthleteId.remove(athleteId);

            String trainerId = planToDelete.getAuthorId();
            if(trainerId != null) {
                List<String> plansId = plansIdByTrainerId.get(trainerId);

                if(plansId != null) plansId.remove(planId);
            }

            planById.remove(planId);
        }

    }

    @Override
    public Optional<WorkoutPlan> findPlanById(String planId) {
        Objects.requireNonNull(planId, "planId cannot be null");

        return Optional.ofNullable(planById.get(planId)).map(WorkoutPlan::new);
    }

    @Override
    public synchronized void assignPlanToAthlete(String planId, String athleteId) {
        Objects.requireNonNull(planId, "planId cannot be null");
        Objects.requireNonNull(athleteId, "athleteId cannot be null");

        WorkoutPlan plan = planById.get(planId);
        if(plan != null) plan.assignTo(athleteId);

        planIdByAthleteId.put(athleteId, planId);

    }

    @Override
    public Optional<WorkoutPlan> findAssignedPlanByAthleteId(String athleteId) {
        Objects.requireNonNull(athleteId, "athleteId cannot be null");

        String planId = planIdByAthleteId.get(athleteId);
        if(planId == null) return Optional.empty();

        return Optional
                .ofNullable(planById.get(planId))
                .map(WorkoutPlan::new);
    }

    @Override
    public List<WorkoutPlan> findPlansByTrainerId(String trainerId) {
        Objects.requireNonNull(trainerId, "trainerId cannot be null");

        List<String> plans = plansIdByTrainerId.get(trainerId);
        if(plans == null || plans.isEmpty()) {
            return List.of();
        }

        List<WorkoutPlan> result = new ArrayList<>();
        for(String id: plans){
            WorkoutPlan plan = planById.get(id);
            if(plan != null) result.add(new WorkoutPlan(plan));
        }

        return result;
    }

}
