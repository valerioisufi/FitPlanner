package com.example.fitplannerserver.mapper;

import com.example.fitplannercommon.ExerciseLogDTO;
import com.example.fitplannercommon.ExerciseSetDTO;
import com.example.fitplannercommon.SessionLogDTO;
import com.example.fitplannerserver.model.log.ExerciseLog;
import com.example.fitplannerserver.model.log.SessionLog;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class LogMapper {

    private LogMapper(){}

    public static SessionLogDTO toBean(SessionLog entity) {
        if (entity == null) return null;

        SessionLogDTO bean = new SessionLogDTO();
        bean.setUserId(entity.getUserId());
        bean.setNotes(entity.getNotes());

        if (entity.getStatus() != null) {
            bean.setStatus(SessionLogDTO.SessionStatus.valueOf(entity.getStatus().name()));
        }

        if (entity.getDate() != null) {
            long dateInMillis = entity.getDate()
                    .atZone(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli();
            bean.setDate(dateInMillis);
        }

        if (entity.getExerciseLogs() != null) {
            List<ExerciseLogDTO> exerciseLogDTOs = new ArrayList<>();
            for (ExerciseLog exLog : entity.getExerciseLogs()) {
                exerciseLogDTOs.add(toBean(exLog));
            }
            bean.setExerciseLogs(exerciseLogDTOs);
        }

        return bean;
    }

    public static ExerciseLogDTO toBean(ExerciseLog entity) {
        if (entity == null) return null;

        ExerciseLogDTO bean = new ExerciseLogDTO();
        bean.setName(entity.getName());
        bean.setExerciseId(entity.getExerciseId());
        bean.setRpe(entity.getRpe());
        bean.setNotes(entity.getNotes());

        if (entity.getSets() != null) {
            List<ExerciseSetDTO> setBeans = new ArrayList<>();
            for (ExerciseLog.ExerciseSet set : entity.getSets()) {
                setBeans.add(new ExerciseSetDTO(set.reps(), set.load()));
            }
            bean.setSets(setBeans);
        }

        return bean;
    }

    public static SessionLog toEntity(String userId, SessionLogDTO bean) {

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

        for (ExerciseLogDTO exBean : bean.getExerciseLogs()) {
            entity.addExerciseLog(toEntity(exBean));
        }

        return entity;
    }

    public static ExerciseLog toEntity(ExerciseLogDTO bean) {

        List<ExerciseLog.ExerciseSet> sets = new ArrayList<>();
        for (ExerciseSetDTO setBean : bean.getSets()) {
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
