package com.example.fitplannerclient.serializer;

import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
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
            throw new RuntimeException("Error deserializing plan node JSON", e);
        }
    }

    private PlanNode deserializeNode(PlanNodeDTO dto) {
        if (dto == null) {
            return null;
        }

        switch (dto.getType()) {
            case EXERCISE:
                ExerciseNode exerciseNode = new ExerciseNode();
                exerciseNode.setResourceId(dto.getResourceId());
                if (dto.getModifiers() != null) {
                    for (PlanNodeDTO.Modifier modDto : dto.getModifiers()) {
                        exerciseNode.addModifier(new ExerciseModifier(
                                ModifierType.valueOf(modDto.type().name()),
                                modDto.value()
                        ));
                    }
                }
                return exerciseNode;

            case BLOCK:
                Block block = new Block(dto.getName());
                if (dto.getChildren() != null) {
                    for (PlanNodeDTO childDto : dto.getChildren()) {
                        block.addNode(deserializeNode(childDto));
                    }
                }
                return block;

            case PROTOCOL_BLOCK:
                // Il semanticType viene memorizzato nel campo name del DTO
                ProtocolBlock protocolBlock = new ProtocolBlock(dto.getName(), null, null, null);
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

            case FLOW_DECORATOR:
                if (dto.getFlowDecorator() == null) {
                    throw new IllegalArgumentException("Flow decorator specified but getFlowDecorator() is null");
                }
                if (dto.getChildren() == null || dto.getChildren().isEmpty()) {
                    throw new IllegalArgumentException("Flow decorator must have a wrapped node as child");
                }
                PlanNode wrappedNode = deserializeNode(dto.getChildren().get(0));
                
                PlanNodeDTO.FlowDecorator fd = dto.getFlowDecorator();
                switch (fd.type()) {
                    case LOOP:
                        return new LoopDecorator(wrappedNode, fd.value());
                    case REST:
                        return new RestDecorator(wrappedNode, fd.value());
                    case TIME_LIMIT:
                        return new TimeLimitDecorator(wrappedNode, fd.value());
                    case INTERVAL:
                        return new IntervalDecorator(wrappedNode, fd.value());
                    case PROGRESSION:
                        return new ProgressionDecorator(wrappedNode, fd.value());
                    default:
                        throw new IllegalArgumentException("Unknown flow decorator type: " + fd.type());
                }

            default:
                throw new IllegalArgumentException("Unknown node type: " + dto.getType());
        }
    }

    public PlanNode toEntity(PlanNodeBean bean) {
        if (bean == null) {
            return null;
        }

        PlanNode coreNode;
        switch (bean.getType()) {
            case EXERCISE:
                ExerciseNode exerciseNode = new ExerciseNode();
                exerciseNode.setResourceId(bean.getResourceId());
                if (bean.getModifiers() != null) {
                    for (ExerciseModifierBean modBean : bean.getModifiers()) {
                        exerciseNode.addModifier(new ExerciseModifier(
                                ModifierType.valueOf(modBean.getName()),
                                modBean.getValue()
                        ));
                    }
                }
                coreNode = exerciseNode;
                break;

            case BLOCK:
                Block block = new Block(bean.getName());
                if (bean.getChildren() != null) {
                    for (PlanNodeBean childBean : bean.getChildren()) {
                        block.addNode(toEntity(childBean));
                    }
                }
                coreNode = block;
                break;

            case PROTOCOL_BLOCK:
                ProtocolBlock protocolBlock = new ProtocolBlock(bean.getName(), null, null, null);
                if (bean.getParameters() != null) {
                    for (Map.Entry<String, String> entry : bean.getParameters().entrySet()) {
                        protocolBlock.setParameter(entry.getKey(), entry.getValue());
                    }
                }
                if (bean.getChildren() != null) {
                    for (PlanNodeBean childBean : bean.getChildren()) {
                        protocolBlock.addNode(toEntity(childBean));
                    }
                }
                coreNode = protocolBlock;
                break;

            default:
                throw new IllegalArgumentException("Unknown bean type: " + bean.getType());
        }

        // Applica i decoratori in ordine inverso (dal più interno al più esterno)
        PlanNode currentNode = coreNode;
        if (bean.getFlowDecorators() != null) {
            for (int i = bean.getFlowDecorators().size() - 1; i >= 0; i--) {
                FlowDecoratorBean decBean = bean.getFlowDecorators().get(i);
                switch (decBean.getType()) {
                    case LOOP:
                        currentNode = new LoopDecorator(currentNode, decBean.getValue());
                        break;
                    case REST:
                        currentNode = new RestDecorator(currentNode, decBean.getValue());
                        break;
                    case TIME_LIMIT:
                        currentNode = new TimeLimitDecorator(currentNode, decBean.getValue());
                        break;
                    case INTERVAL:
                        currentNode = new IntervalDecorator(currentNode, decBean.getValue());
                        break;
                    case PROGRESSION:
                        currentNode = new ProgressionDecorator(currentNode, decBean.getValue());
                        break;
                }
            }
        }

        return currentNode;
    }
}
