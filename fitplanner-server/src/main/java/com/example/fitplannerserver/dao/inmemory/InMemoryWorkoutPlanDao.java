package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.WorkoutPlanDao;
import com.example.fitplannerserver.model.plan.WorkoutPlan;
import com.example.fitplannerserver.model.plan.WorkoutSession;

import java.util.List;

public class InMemoryWorkoutPlanDao implements WorkoutPlanDao{

    private static class Wrapper {
        public static final InMemoryWorkoutPlanDao INSTANCE = new InMemoryWorkoutPlanDao();
    }

    private InMemoryWorkoutPlanDao(){}

    public static InMemoryWorkoutPlanDao getInstance() {
        return Wrapper.INSTANCE;
    }

    @Override
    public void savePlan(WorkoutPlan plan) {

    }

    @Override
    public void updatePlan(WorkoutPlan plan) {

    }

    @Override
    public void deletePlan(String planUuid) {

    }

    @Override
    public WorkoutPlan findPlanById(String planUuid) {
        return null;
    }

    @Override
    public void assignPlanToAthlete(String planUuid, String athleteUuid) {

    }

    @Override
    public WorkoutPlan findAssignedPlanByAthleteId(String athleteUuid) {
        return null;
    }

    @Override
    public List<WorkoutPlan> findPlansByTrainerId(String trainerUuid) {
        return List.of();
    }

    @Override
    public void saveSession(String planUuid, WorkoutSession session) {

    }

    @Override
    public void updateSession(WorkoutSession session) {

    }

    @Override
    public void deleteSession(String sessionUuid) {

    }

    @Override
    public WorkoutSession findSessionById(String sessionUuid) {
        return null;
    }

}
