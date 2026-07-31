package com.example.fitplannerserver.dao.inmemory;

import com.example.fitplannerserver.dao.WorkoutSessionDao;
import com.example.fitplannerserver.model.plan.WorkoutSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryWorkoutSessionDao implements WorkoutSessionDao {

    private static final String NULL_PLAN_ID_MSG="planId cannot be null";

    // key = planId
    private final Map<String, List<WorkoutSession>> sessionsMap = new ConcurrentHashMap<>();

    @Override
    public void saveSessionsForPlan(String planId, List<WorkoutSession> sessions) {
        Objects.requireNonNull(planId, NULL_PLAN_ID_MSG);
        Objects.requireNonNull(sessions, "sessions cannot be null");

        sessionsMap.put(planId, sessions.stream().map(WorkoutSession::new).toList());
    }

    @Override
    public List<WorkoutSession> findSessionsByPlanId(String planId) {
        Objects.requireNonNull(planId, NULL_PLAN_ID_MSG);

        return sessionsMap.getOrDefault(planId, List.of()).stream().map(WorkoutSession::new).toList();
    }

    @Override
    public Optional<WorkoutSession> findSessionByPlanIdAndDay(String planId, int day) {
        Objects.requireNonNull(planId, NULL_PLAN_ID_MSG);

        return findSessionsByPlanId(planId).stream()
                .filter(s -> s.getDay() == day)
                .findFirst();
    }

    @Override
    public void deleteSessionsByPlanId(String planId) {
        Objects.requireNonNull(planId, NULL_PLAN_ID_MSG);

        sessionsMap.remove(planId);
    }
}
