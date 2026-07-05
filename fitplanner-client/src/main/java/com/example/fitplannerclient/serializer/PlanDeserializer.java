package com.example.fitplannerclient.serializer;

import com.example.fitplannerclient.bean.plan.ExerciseModifierBean;
import com.example.fitplannerclient.bean.plan.FlowDecoratorBean;
import com.example.fitplannerclient.bean.plan.PlanNodeBean;
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
            case PROTOCOL_BLOCK:
                return deserializeProtocolBlockNode(dto);
            case FLOW_DECORATOR:
                return deserializeFlowDecorator(dto);
            default:
                throw new IllegalArgumentException("Unknown node type: " + dto.getType());
        }
    }

    private ExerciseNode deserializeExerciseNode(PlanNodeDTO dto) {
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
            case LOOP -> new LoopDecorator(wrappedNode, fd.value());
            case REST -> new RestDecorator(wrappedNode, fd.value());
            case TIME_LIMIT -> new TimeLimitDecorator(wrappedNode, fd.value());
            case INTERVAL -> new IntervalDecorator(wrappedNode, fd.value());
            case PROGRESSION -> new ProgressionDecorator(wrappedNode, fd.value());
            default -> throw new IllegalArgumentException("Unknown flow decorator type: " + fd.type());
        };
    }

    public PlanNode toEntity(PlanNodeBean bean) {
        if (bean == null) {
            return null;
        }

        PlanNode coreNode;
        switch (bean.getType()) {
            case EXERCISE:
                coreNode = toEntityExerciseNode(bean);
                break;
            case BLOCK:
                coreNode = toEntityBlockNode(bean);
                break;
            case PROTOCOL_BLOCK:
                coreNode = toEntityProtocolBlockNode(bean);
                break;
            default:
                throw new IllegalArgumentException("Unknown bean type: " + bean.getType());
        }

        // Applica i decoratori in ordine inverso (dal più interno al più esterno)
        PlanNode currentNode = coreNode;
        if (bean.getFlowDecorators() != null) {
            for (int i = bean.getFlowDecorators().size() - 1; i >= 0; i--) {
                FlowDecoratorBean decBean = bean.getFlowDecorators().get(i);
                currentNode = switch (decBean.getType()) {
                    case LOOP -> new LoopDecorator(currentNode, decBean.getValue());
                    case REST -> new RestDecorator(currentNode, decBean.getValue());
                    case TIME_LIMIT -> new TimeLimitDecorator(currentNode, decBean.getValue());
                    case INTERVAL -> new IntervalDecorator(currentNode, decBean.getValue());
                    case PROGRESSION -> new ProgressionDecorator(currentNode, decBean.getValue());
                };
            }
        }

        return currentNode;
    }

    private ExerciseNode toEntityExerciseNode(PlanNodeBean bean) {
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
        return exerciseNode;
    }

    private Block toEntityBlockNode(PlanNodeBean bean) {
        Block block = new Block(bean.getName());
        if (bean.getChildren() != null) {
            for (PlanNodeBean childBean : bean.getChildren()) {
                block.addNode(toEntity(childBean));
            }
        }
        return block;
    }

    private ProtocolBlock toEntityProtocolBlockNode(PlanNodeBean bean) {
        ProtocolBlockFactory factory = new ProtocolBlockFactory();
        ProtocolBlock protocolBlock = factory.create(bean.getName());
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
        return protocolBlock;
    }
}
