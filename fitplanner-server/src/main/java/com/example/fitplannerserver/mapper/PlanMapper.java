package com.example.fitplannerserver.mapper;

import com.example.fitplannercommon.WorkoutPlanBean;
import com.example.fitplannercommon.WorkoutSessionBean;
import com.example.fitplannerserver.model.plan.WorkoutPlan;
import com.example.fitplannerserver.model.plan.WorkoutSession;

import java.util.ArrayList;
import java.util.List;

public class PlanMapper {

    private PlanMapper(){}

    public static WorkoutPlanBean toBean(WorkoutPlan entity){
        WorkoutPlanBean bean = new WorkoutPlanBean();

        bean.setPlanId(entity.getPlanId());
        bean.setName(entity.getTitle());

        List<WorkoutSessionBean> sessionBeans = new ArrayList<>();
        for(int day: entity.getSessionsDay()){
            sessionBeans.add(toBean(entity.getSession(day)));
        }
        bean.setWorkoutSessions(sessionBeans);
        return bean;
    }

    private static WorkoutSessionBean toBean(WorkoutSession entity){
        WorkoutSessionBean bean = new WorkoutSessionBean(
                entity.getTitle(),
                entity.getContent(),
                entity.getDay()
        );

        return bean;
    }

    public static WorkoutPlan toEntity(WorkoutPlanBean bean, String planId){
        WorkoutPlan entity = new WorkoutPlan(planId, bean.getName(), bean.getCycleLength());

        for(WorkoutSessionBean sessionBean: bean.getWorkoutSessions()){
            entity.addSession(toEntity(sessionBean));
        }

        return entity;
    }

    private static WorkoutSession toEntity(WorkoutSessionBean bean){

        return new WorkoutSession(
                bean.getName(),
                bean.getContent(),
                bean.getDay()
        );

    }

}
