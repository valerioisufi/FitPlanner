package com.example.fitplannerclient.serializer;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannercommon.WorkoutPlanDTO;
import com.example.fitplannercommon.WorkoutSessionDTO;
import tools.jackson.databind.ObjectMapper;

public class PlanDeserializer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WorkoutPlan toEntity(WorkoutPlanDTO planDto) {
        WorkoutPlan workoutPlan = new WorkoutPlan(
                planDto.getName(),
                planDto.getPlanId()
        );
        workoutPlan.setCycleLength(planDto.getCycleLength());

        for (WorkoutSessionDTO sessionDto : planDto.getWorkoutSessions()) {
            PlanNode root = deserialize(sessionDto.getContent());

            WorkoutSession session = new WorkoutSession(
                    sessionDto.getName(),
                    sessionDto.getDay(),
                    root
            );

            workoutPlan.addSession(session);
        }

        return workoutPlan;
    }

    private PlanNode deserialize(String json) {
        PlanNodeDTO planNodeDTO = objectMapper.readValue(json, PlanNodeDTO.class);

//        for ()

        return null;
    }
}
