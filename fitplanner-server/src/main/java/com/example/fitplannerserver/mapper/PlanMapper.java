package com.example.fitplannerserver.mapper;

import com.example.fitplannercommon.WorkoutPlanDTO;
import com.example.fitplannercommon.WorkoutPlanSummaryDTO;
import com.example.fitplannercommon.WorkoutSessionDTO;
import com.example.fitplannerserver.model.plan.WorkoutPlan;
import com.example.fitplannerserver.model.plan.WorkoutSession;

import java.util.ArrayList;
import java.util.List;

public class PlanMapper {

    private PlanMapper(){}

    public static WorkoutPlanDTO toDto(WorkoutPlan entity){
        WorkoutPlanDTO dto = new WorkoutPlanDTO();

        dto.setPlanId(entity.getPlanId());
        dto.setName(entity.getTitle());
        dto.setCycleLength(entity.getCycleLength());

        List<WorkoutSessionDTO> sessionBeans = new ArrayList<>();
        for(int day: entity.getSessionsDay()){
            sessionBeans.add(toDto(entity.getSession(day)));
        }
        dto.setWorkoutSessions(sessionBeans);
        return dto;
    }

    private static WorkoutSessionDTO toDto(WorkoutSession entity){
        return new WorkoutSessionDTO(
                entity.getTitle(),
                entity.getContent(),
                entity.getDay()
        );
    }

    public static WorkoutPlanSummaryDTO toSummaryDto(WorkoutPlan entity){
        WorkoutPlanSummaryDTO dto = new WorkoutPlanSummaryDTO();
        dto.setPlanId(entity.getPlanId());
        dto.setPlanTitle(entity.getTitle());
        dto.setAssignedTo(entity.getAssignedToId());

        return dto;
    }

    public static WorkoutPlan toEntity(WorkoutPlanDTO dto, String planId){
        WorkoutPlan entity = new WorkoutPlan(planId, dto.getName(), dto.getCycleLength());

        for(WorkoutSessionDTO sessionBean: dto.getWorkoutSessions()){
            entity.addSession(toEntity(sessionBean));
        }

        return entity;
    }

    private static WorkoutSession toEntity(WorkoutSessionDTO dto){

        return new WorkoutSession(
                dto.getName(),
                dto.getContent(),
                dto.getDay()
        );

    }

}
