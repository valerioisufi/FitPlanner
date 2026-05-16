package com.example.fitplannerserver.mapper;

import com.example.fitplannercommon.ExerciseLogBean;
import com.example.fitplannercommon.ExerciseSetBean;
import com.example.fitplannercommon.SessionLogBean;
import com.example.fitplannerserver.model.log.ExerciseLog;
import com.example.fitplannerserver.model.log.SessionLog;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class LogMapper {

    private LogMapper(){}

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

    public static SessionLog toEntity(String userId, SessionLogBean bean) {

        SessionLog.SessionStatus status = SessionLog.SessionStatus.valueOf(bean.getStatus().name());

        LocalDateTime date = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(bean.getDate()),
                ZoneOffset.UTC
        );

        SessionLog entity = new SessionLog(
                userId,
                bean.getNotes().trim(),
                status,
                date
        );

        for (ExerciseLogBean exBean : bean.getExerciseLogs()) {
            entity.addExerciseLog(toEntity(exBean));
        }

        return entity;
    }

    public static ExerciseLog toEntity(ExerciseLogBean bean) {

        List<ExerciseLog.ExerciseSet> sets = new ArrayList<>();
        for (ExerciseSetBean setBean : bean.getSets()) {
            sets.add(new ExerciseLog.ExerciseSet(setBean.getReps(), setBean.getLoad()));
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
