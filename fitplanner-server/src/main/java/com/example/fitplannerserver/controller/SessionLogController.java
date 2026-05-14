package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.ExerciseLogBean;
import com.example.fitplannercommon.ExerciseSetBean;
import com.example.fitplannercommon.SessionLogBean;
import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.dao.SessionLogDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.exception.ResourceNotFoundException;
import com.example.fitplannerserver.exception.SystemException;
import com.example.fitplannerserver.exception.UnauthorizedException;
import com.example.fitplannerserver.model.log.ExerciseLog;
import com.example.fitplannerserver.model.log.SessionLog;
import com.example.fitplannerserver.security.IdentityProvider;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SessionLogController {
    private final IdentityProvider identityProvider;

    public SessionLogController(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    public List<SessionLogBean> getFilteredSessionLog(String athleteId, long startDate, long endDate) {
        SessionLogDao sessionLogDao = DaoFactory.getInstance().getSessionLogDao();

        try {
            List<SessionLog> sessionLog = sessionLogDao.findLogsByAthleteIdAndDateRange(athleteId, startDate, endDate);

            List<SessionLogBean> sessionLogBeans = new ArrayList<>();
            for (SessionLog log : sessionLog) {
                sessionLogBeans.add(toBean(log));
            }
            return sessionLogBeans;

        } catch (DaoException e) {
            throw new SystemException("Unable to retrieve session logs");
        }
    }

    public void saveSessionLog(SessionLogBean logBean) {
        SessionLogDao sessionLogDao = DaoFactory.getInstance().getSessionLogDao();

        if(!Objects.equals(identityProvider.getUserId(), logBean.getUserId())){
            throw new UnauthorizedException("User ID in log does not match authenticated user");
        }

        SessionLog sessionLog = toEntity(logBean);

        try {
            sessionLogDao.saveSessionLog(sessionLog);

        } catch (DaoException e) {
            throw new SystemException("Failed to save session log");
        }
    }

    public ExerciseLogBean getLastRecordForExercise(String exerciseId) {
        SessionLogDao sessionLogDao = DaoFactory.getInstance().getSessionLogDao();

        try {
            Optional<SessionLog> sessionLog = sessionLogDao
                    .findMostRecentSessionContainingExercise(
                            identityProvider.getUserId(),
                            exerciseId
                    );

            if (sessionLog.isPresent()) {

                for (ExerciseLog exerciseLog : sessionLog.get().getExerciseLogs()) {
                    if (exerciseLog.getExerciseId().equals(exerciseId)) {
                        return toBean(exerciseLog);
                    }
                }
            }

            throw new ResourceNotFoundException("Exercise not found in any recent session logs");

        } catch (DaoException e) {
            throw new SystemException("Unable to retrieve last record for exercise");
        }

    }


    // mapper methods
    public static SessionLogBean toBean(SessionLog entity) {
        if (entity == null) return null;

        SessionLogBean bean = new SessionLogBean();
        bean.setUserId(entity.getUserId());
        bean.setNotes(entity.getNotes());

        if (entity.getStatus() != null) {
            bean.setStatus(SessionLogBean.SessionStatus.valueOf(entity.getStatus().name()));
        }

        if (entity.getDate() != null) {
            long dateInMillis = entity.getDate()
                    .atZone(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli();
            bean.setDate(dateInMillis);
        }

        if (entity.getExerciseLogs() != null) {
            List<ExerciseLogBean> exerciseLogBeans = new ArrayList<>();
            for (ExerciseLog exLog : entity.getExerciseLogs()) {
                exerciseLogBeans.add(toBean(exLog));
            }
            bean.setExerciseLogs(exerciseLogBeans);
        }

        return bean;
    }

    public static ExerciseLogBean toBean(ExerciseLog entity) {
        if (entity == null) return null;

        ExerciseLogBean bean = new ExerciseLogBean();
        bean.setName(entity.getName());
        bean.setExerciseId(entity.getExerciseId());
        bean.setRpe(entity.getRpe());
        bean.setNotes(entity.getNotes());

        if (entity.getSets() != null) {
            List<ExerciseSetBean> setBeans = new ArrayList<>();
            for (ExerciseLog.ExerciseSet set : entity.getSets()) {
                setBeans.add(new ExerciseSetBean(set.reps(), set.load()));
            }
            bean.setSets(setBeans);
        }

        return bean;
    }

    public static SessionLog toEntity(SessionLogBean bean) {
        if (bean == null) return null;

        SessionLog.SessionStatus status = null;
        if (bean.getStatus() != null) {
            status = SessionLog.SessionStatus.valueOf(bean.getStatus().name());
        }

        LocalDateTime date = null;
        if (bean.getDate() > 0) {
            date = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(bean.getDate()),
                    ZoneOffset.UTC
            );
        }

        SessionLog entity = new SessionLog(
                bean.getUserId(),
                bean.getNotes(),
                status,
                date
        );

        if (bean.getExerciseLogs() != null) {
            for (ExerciseLogBean exBean : bean.getExerciseLogs()) {
                entity.addExerciseLog(toEntity(exBean));
            }
        }

        return entity;
    }

    public static ExerciseLog toEntity(ExerciseLogBean bean) {
        if (bean == null) return null;

        List<ExerciseLog.ExerciseSet> sets = new ArrayList<>();
        if (bean.getSets() != null) {
            for (ExerciseSetBean setBean : bean.getSets()) {
                sets.add(new ExerciseLog.ExerciseSet(setBean.getReps(), setBean.getLoad()));
            }
        }

        return new ExerciseLog(
                bean.getName(),
                bean.getExerciseId(),
                sets,
                bean.getRpe(),
                bean.getNotes()
        );
    }

}
