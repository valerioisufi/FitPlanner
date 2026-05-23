package com.example.fitplannerclient.serializer;

import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import com.example.fitplannercommon.WorkoutPlanDTO;
import com.example.fitplannercommon.WorkoutSessionDTO;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class PlanToBeanVisitor implements WorkoutPlanVisitor {

    private WorkoutPlanBean planBean;
    private final List<WorkoutSessionDTO> sessionDtos = new ArrayList<>();

    private PlanNodeDTO currentNodeDto;

    public WorkoutPlanBean getPlanBean() {
        return this.planBean;
    }

    @Override
    public void visit(WorkoutPlan workoutPlan) {
        this.planBean = new WorkoutPlanBean();
        this.planBean.setName(workoutPlan.getName());
        this.planBean.setCycleLength(workoutPlan.getCycleLength());

        workoutPlan.getSessions().forEach(session ->
                session.accept(this)
        );

//        this.planBean.setSessions(sessionDtos);

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
                                modifier.getName(),
                                modifier.getValue()
                        )
                )
                .toList();

        currentNodeDto.setModifiers(modifierDtos);
    }

    @Override
    public void visit(Block block) {
        currentNodeDto.setType(PlanNodeDTO.NodeType.BLOCK);
        currentNodeDto.setName(block.getTitle());

        PlanNodeDTO thisNodeDto = currentNodeDto;

        for(int i = 0; i < block.getChildrenCount(); i++) {
            PlanNode child = block.getNodeAt(i);

            currentNodeDto = new PlanNodeDTO(); // child node
            thisNodeDto.getChildren().add(currentNodeDto);

            child.accept(this);
        }

    }

    @Override
    public void visit(ProtocolBlock protocolBlock) {
        currentNodeDto.setType(PlanNodeDTO.NodeType.PROTOCOL_BLOCK);

    }

    @Override
    public void visit(LoopDecorator loopDecorator) {
        visitFlowDecorator(
                loopDecorator,
                PlanNodeDTO.FlowDecoratorType.LOOP,
                String.valueOf(loopDecorator.getRounds())
        );

    }

    @Override
    public void visit(RestDecorator restDecorator) {
        visitFlowDecorator(
                restDecorator,
                PlanNodeDTO.FlowDecoratorType.REST,
                String.valueOf(restDecorator.getRestDuration())
        );

    }

    @Override
    public void visit(TimeLimitDecorator timeLimitDecorator) {
        visitFlowDecorator(
                timeLimitDecorator,
                PlanNodeDTO.FlowDecoratorType.TIME_LIMIT,
                String.valueOf(timeLimitDecorator.getTimeLimit())
        );

    }

    @Override
    public void visit(ProgressionDecorator progressionDecorator) {
        visitFlowDecorator(
                progressionDecorator,
                PlanNodeDTO.FlowDecoratorType.PROGRESSION,
                progressionDecorator.getProgression()
        );

    }

    @Override
    public void visit(IntervalDecorator intervalDecorator) {
        visitFlowDecorator(
                intervalDecorator,
                PlanNodeDTO.FlowDecoratorType.INTERVAL,
                String.valueOf(intervalDecorator.getIntervalDuration())
        );
    }

    private void visitFlowDecorator(FlowDecorator flowDecorator, PlanNodeDTO.FlowDecoratorType decoratorType, String parameter) {
        currentNodeDto.setType(PlanNodeDTO.NodeType.FLOW_DECORATOR);
        currentNodeDto.setFlowDecorator(new PlanNodeDTO.FlowDecorator(
                decoratorType,
                parameter
        ));

        PlanNodeDTO previousNodeDto = currentNodeDto;
        currentNodeDto = new PlanNodeDTO(); // wrapped node
        previousNodeDto.getChildren().add(currentNodeDto);

        flowDecorator.getWrappedNode().accept(this);
    }
}
