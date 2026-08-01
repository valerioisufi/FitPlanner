package com.example.fitplannerclient.serializer;

import com.example.fitplannerclient.entity.plan.block.CompositeNode;
import com.example.fitplannerclient.entity.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import com.example.fitplannercommon.WorkoutPlanDTO;
import com.example.fitplannercommon.WorkoutSessionDTO;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlanToDtoVisitor implements WorkoutPlanVisitor {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private WorkoutPlanDTO planDto;
    private final List<WorkoutSessionDTO> sessionDtos = new ArrayList<>();

    private PlanNodeDTO currentNodeDto;

    public WorkoutPlanDTO getPlanDto() {
        return this.planDto;
    }

    @Override
    public void visit(WorkoutPlan workoutPlan) {
        this.planDto = new WorkoutPlanDTO();
        this.planDto.setPlanId(workoutPlan.getPlanId());
        this.planDto.setName(workoutPlan.getName());
        this.planDto.setCycleLength(workoutPlan.getCycleLength());

        workoutPlan.getSessions().forEach(session ->
                session.accept(this)
        );

        this.planDto.setWorkoutSessions(sessionDtos);

    }

    @Override
    public void visit(WorkoutSession workoutSession) {
        WorkoutSessionDTO sessionDto = new WorkoutSessionDTO();
        sessionDto.setName(workoutSession.getName());
        sessionDto.setDay(workoutSession.getDay());

        sessionDtos.add(sessionDto);

        PlanNode root = workoutSession.getRoot();
        if(root != null) {
            currentNodeDto = new PlanNodeDTO();
            root.accept(this);

            String json = objectMapper.writeValueAsString(currentNodeDto);
            sessionDto.setContent(json);
        }

    }

    @Override
    public void visit(ExerciseNode exerciseNode) {
        currentNodeDto.setType(PlanNodeDTO.NodeType.EXERCISE);
        currentNodeDto.setResourceId(exerciseNode.getResourceId());

        List<PlanNodeDTO.Modifier> modifierDtos = exerciseNode.getModifiers()
                .stream()
                .map(modifier ->
                        new PlanNodeDTO.Modifier(
                                PlanNodeDTO.ModifierType.valueOf(modifier.getType().name()),
                                modifier.getValue()
                        )
                )
                .toList();

        currentNodeDto.setModifiers(modifierDtos);
    }

    @Override
    public void visit(CompositeNode compositeNode) {
        String name = compositeNode.getName().orElse("Senza nome");
        PlanNodeDTO.NodeType nodeType = switch(compositeNode.getType()) {
            case BLOCK -> PlanNodeDTO.NodeType.BLOCK;
            case PROTOCOL -> PlanNodeDTO.NodeType.PROTOCOL;
        };

        currentNodeDto.setType(nodeType);
        currentNodeDto.setName(name);
        currentNodeDto.setParameters(compositeNode.getParameters() != null ? new HashMap<>(compositeNode.getParameters()) : new HashMap<>());
        currentNodeDto.setResourceId(compositeNode.getProtocolType().map(Enum::name).orElse(null));

        PlanNodeDTO thisNodeDto = currentNodeDto;

        for (int i = 0; i < compositeNode.getChildrenCount(); i++) {
            PlanNode child = compositeNode.getNodeAt(i);

            currentNodeDto = new PlanNodeDTO();
            thisNodeDto.getChildren().add(currentNodeDto);

            child.accept(this);
        }

        currentNodeDto = thisNodeDto;
    }

    @Override
    public void visit(FlowDecorator flowDecorator) {
        currentNodeDto.setType(PlanNodeDTO.NodeType.FLOW_DECORATOR);
        currentNodeDto.setFlowDecorator(new PlanNodeDTO.FlowDecorator(
                serializeFlowDecoratorType(flowDecorator.getType()),
                flowDecorator.getSerializedValue()
        ));

        PlanNodeDTO previousNodeDto = currentNodeDto;
        currentNodeDto = new PlanNodeDTO(); // wrapped node
        previousNodeDto.getChildren().add(currentNodeDto);

        flowDecorator.getWrappedNode().accept(this);

        currentNodeDto = previousNodeDto;
    }

    private PlanNodeDTO.FlowDecoratorType serializeFlowDecoratorType(FlowDecoratorType flowDecoratorType) {
        return switch (flowDecoratorType) {
            case LOOP -> PlanNodeDTO.FlowDecoratorType.LOOP;
            case REST -> PlanNodeDTO.FlowDecoratorType.REST;
            case TIME_LIMIT -> PlanNodeDTO.FlowDecoratorType.TIME_LIMIT;
            case INTERVAL -> PlanNodeDTO.FlowDecoratorType.INTERVAL;
            case PROGRESSION -> PlanNodeDTO.FlowDecoratorType.PROGRESSION;
        };
    }


}
