package com.example.fitplannerserver.mapper;

import com.example.fitplannercommon.WorkoutPlanDTO;
import com.example.fitplannercommon.WorkoutSessionDTO;
import com.example.fitplannerserver.model.plan.WorkoutPlan;
import com.example.fitplannerserver.model.plan.WorkoutSession;

import java.util.ArrayList;
import java.util.List;

public class PlanMapper {

    private PlanMapper(){}

    public static WorkoutPlanDTO toBean(WorkoutPlan entity){
        WorkoutPlanDTO bean = new WorkoutPlanDTO();

        bean.setPlanId(entity.getPlanId());
        bean.setName(entity.getTitle());

        List<WorkoutSessionDTO> sessionBeans = new ArrayList<>();
        for(int day: entity.getSessionsDay()){
            sessionBeans.add(toBean(entity.getSession(day)));
        }
        bean.setWorkoutSessions(sessionBeans);
        return bean;
    }

    private static WorkoutSessionDTO toBean(WorkoutSession entity){
        WorkoutSessionDTO bean = new WorkoutSessionDTO(
                entity.getTitle(),
                entity.getContent(),
                entity.getDay()
        );

        return bean;
    }

    public static WorkoutPlan toEntity(WorkoutPlanDTO bean, String planId){
        WorkoutPlan entity = new WorkoutPlan(planId, bean.getName(), bean.getCycleLength());

        for(WorkoutSessionDTO sessionBean: bean.getWorkoutSessions()){
            entity.addSession(toEntity(sessionBean));
        }

        return entity;
    }

    private static WorkoutSession toEntity(WorkoutSessionDTO bean){

        return new WorkoutSession(
                bean.getName(),
                bean.getContent(),
                bean.getDay()
        );

    }

}
