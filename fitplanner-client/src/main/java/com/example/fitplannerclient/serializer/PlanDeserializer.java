package com.example.fitplannerclient.serializer;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlockFactory;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseModifier;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import com.example.fitplannerclient.entity.plan.exercise.ModifierType;
import com.example.fitplannercommon.WorkoutPlanDTO;
import com.example.fitplannercommon.WorkoutSessionDTO;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

public class PlanDeserializer {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final FlowDecoratorFactory flowDecoratorFactory = new FlowDecoratorFactory();

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

    public PlanNode deserialize(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            PlanNodeDTO planNodeDTO = objectMapper.readValue(json, PlanNodeDTO.class);
            return deserializeNode(planNodeDTO);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error deserializing plan node JSON", e);
        }
    }

    private PlanNode deserializeNode(PlanNodeDTO dto) {
        if (dto == null) {
            return null;
        }

        switch (dto.getType()) {
            case EXERCISE:
                return deserializeExerciseNode(dto);
            case BLOCK:
                return deserializeBlockNode(dto);
            case PROTOCOL:
                return deserializeProtocolBlockNode(dto);
            case FLOW_DECORATOR:
                return deserializeFlowDecorator(dto);
            default:
                throw new IllegalArgumentException("Unknown node type: " + dto.getType());
        }
    }

    private ExerciseNode deserializeExerciseNode(PlanNodeDTO dto) {
        ExerciseNode exerciseNode = new ExerciseNode();
        exerciseNode.setExerciseInfo(dto.getResourceId(), dto.getName());
        if (dto.getModifiers() != null) {
            for (PlanNodeDTO.Modifier modDto : dto.getModifiers()) {
                exerciseNode.addModifier(new ExerciseModifier(
                        ModifierType.valueOf(modDto.type().name()),
                        modDto.value()
                ));
            }
        }
        return exerciseNode;
    }

    private Block deserializeBlockNode(PlanNodeDTO dto) {
        Block block = new Block(dto.getName());
        if (dto.getChildren() != null) {
            for (PlanNodeDTO childDto : dto.getChildren()) {
                block.addNode(deserializeNode(childDto));
            }
        }
        return block;
    }

    private ProtocolBlock deserializeProtocolBlockNode(PlanNodeDTO dto) {
        // Il semanticType viene memorizzato nel campo name del DTO
        ProtocolBlockFactory factory = new ProtocolBlockFactory();
        ProtocolBlock protocolBlock = factory.create(dto.getName());
        if (dto.getParameters() != null) {
            for (Map.Entry<String, String> entry : dto.getParameters().entrySet()) {
                protocolBlock.setParameter(entry.getKey(), entry.getValue());
            }
        }
        if (dto.getChildren() != null) {
            for (PlanNodeDTO childDto : dto.getChildren()) {
                protocolBlock.addNode(deserializeNode(childDto));
            }
        }
        return protocolBlock;
    }

    private PlanNode deserializeFlowDecorator(PlanNodeDTO dto) {
        if (dto.getFlowDecorator() == null) {
            throw new IllegalArgumentException("Flow decorator specified but getFlowDecorator() is null");
        }
        if (dto.getChildren() == null || dto.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Flow decorator must have a wrapped node as child");
        }
        PlanNode wrappedNode = deserializeNode(dto.getChildren().getFirst());

        PlanNodeDTO.FlowDecorator fd = dto.getFlowDecorator();
        return switch (fd.type()) {
            case LOOP -> flowDecoratorFactory.createLoopDecorator(wrappedNode, fd.value());
            case REST -> flowDecoratorFactory.createRestDecorator(wrappedNode, fd.value());
            case TIME_LIMIT -> flowDecoratorFactory.createTimeLimitDecorator(wrappedNode, fd.value());
            case INTERVAL -> flowDecoratorFactory.createIntervalDecorator(wrappedNode, fd.value());
            case PROGRESSION -> flowDecoratorFactory.createProgressionDecorator(wrappedNode, fd.value());
        };
    }

}
