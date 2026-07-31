package com.example.fitplannerserver.dao;

import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.model.plan.WorkoutSession;

import java.util.List;
import java.util.Optional;

public interface WorkoutSessionDao {

    void saveSessionsForPlan(String planId, List<WorkoutSession> sessions) throws DaoException;

    List<WorkoutSession> findSessionsByPlanId(String planId) throws DaoException;

    Optional<WorkoutSession> findSessionByPlanIdAndDay(String planId, int day) throws DaoException;

    void deleteSessionsByPlanId(String planId) throws DaoException;
}
