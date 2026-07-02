package com.example.fitplannerclient.serializer;

import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.entity.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import com.example.fitplannerclient.util.IDGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.UnaryOperator;

public class PlanToBeanVisitor implements WorkoutPlanVisitor {

    private WorkoutPlanBean planBean;
    private final List<WorkoutSessionBean> sessionBeans = new ArrayList<>();

    private PlanNodeBean currentPlanNodeBean;
    private final List<FlowDecoratorBean> accumulatedDecorators = new ArrayList<>();

    private final UnaryOperator<String> nameResolver;

    public PlanToBeanVisitor() {
        this.nameResolver = id -> "Esercizio Sconosciuto";
    }

    public PlanToBeanVisitor(UnaryOperator<String> nameResolver) {
        this.nameResolver = nameResolver;
    }

    public WorkoutPlanBean getPlanBean() {
        return this.planBean;
    }

    public PlanNodeBean getCurrentPlanNodeBean() {
        return this.currentPlanNodeBean;
    }

    public List<FlowDecoratorBean> getAccumulatedDecorators() {
        return this.accumulatedDecorators;
    }

    @Override
    public void visit(WorkoutPlan workoutPlan) {
        this.planBean = new WorkoutPlanBean();
        this.planBean.setId(workoutPlan.getPlanId());
        this.planBean.setName(workoutPlan.getName());
        this.planBean.setCycleLength(workoutPlan.getCycleLength());
        this.planBean.setSessions(sessionBeans);

        workoutPlan.getSessions().forEach(session ->
                session.accept(this)
        );
    }

    @Override
    public void visit(WorkoutSession workoutSession) {
        WorkoutSessionBean sessionBean = new WorkoutSessionBean();
        sessionBean.setName(workoutSession.getName());
        sessionBean.setDay(workoutSession.getDay());

        sessionBeans.add(sessionBean);

        PlanNode root = workoutSession.getRoot();
        if (root != null) {
            currentPlanNodeBean = null;
            accumulatedDecorators.clear();
            root.accept(this);
            sessionBean.setPlanRoot(currentPlanNodeBean);
        }
    }

    @Override
    public void visit(ExerciseNode exerciseNode) {
        String name = nameResolver.apply(exerciseNode.getResourceId());
        PlanNodeBean nodeBean = new PlanNodeBean(exerciseNode.getId(), name, NodeType.EXERCISE);
        nodeBean.setResourceId(exerciseNode.getResourceId());

        List<ExerciseModifierBean> modifierBeans = exerciseNode.getModifiers()
                .stream()
                .map(mod -> new ExerciseModifierBean(
                        IDGenerator.generateUUID(),
                        mod.getType().name(),
                        mod.getValue()
                ))
                .toList();

        nodeBean.setModifiers(new ArrayList<>(modifierBeans));
        nodeBean.setFlowDecorators(new ArrayList<>(accumulatedDecorators));
        accumulatedDecorators.clear();

        currentPlanNodeBean = nodeBean;
    }

    @Override
    public void visit(Block block) {
        PlanNodeBean nodeBean = new PlanNodeBean(block.getId(), block.getTitle(), NodeType.BLOCK);
        nodeBean.setFlowDecorators(new ArrayList<>(accumulatedDecorators));
        accumulatedDecorators.clear();

        for (int i = 0; i < block.getChildrenCount(); i++) {
            PlanNode child = block.getNodeAt(i);
            
            // I figli partono con un accumulatore pulito
            accumulatedDecorators.clear();
            
            child.accept(this);
            nodeBean.addChild(currentPlanNodeBean);
        }

        currentPlanNodeBean = nodeBean;
    }

    @Override
    public void visit(ProtocolBlock protocolBlock) {
        PlanNodeBean nodeBean = new PlanNodeBean(protocolBlock.getId(), protocolBlock.getSemanticType(), NodeType.PROTOCOL_BLOCK);
        nodeBean.setFlowDecorators(new ArrayList<>(accumulatedDecorators));
        nodeBean.setParameters(protocolBlock.getParameters() != null ? new HashMap<>(protocolBlock.getParameters()) : new HashMap<>());
        accumulatedDecorators.clear();

        for (int i = 0; i < protocolBlock.getChildrenCount(); i++) {
            PlanNode child = protocolBlock.getNodeAt(i);
            
            // I figli partono con un accumulatore pulito
            accumulatedDecorators.clear();
            
            child.accept(this);
            nodeBean.addChild(currentPlanNodeBean);
        }

        currentPlanNodeBean = nodeBean;
    }

    @Override
    public void visit(LoopDecorator loopDecorator) {
        accumulatedDecorators.add(new FlowDecoratorBean(
                loopDecorator.getId(),
                FlowDecoratorType.LOOP,
                loopDecorator.getRoundsExpression()
        ));
        loopDecorator.getWrappedNode().accept(this);
    }

    @Override
    public void visit(RestDecorator restDecorator) {
        accumulatedDecorators.add(new FlowDecoratorBean(
                restDecorator.getId(),
                FlowDecoratorType.REST,
                restDecorator.getRestDuration()
        ));
        restDecorator.getWrappedNode().accept(this);
    }

    @Override
    public void visit(TimeLimitDecorator timeLimitDecorator) {
        accumulatedDecorators.add(new FlowDecoratorBean(
                timeLimitDecorator.getId(),
                FlowDecoratorType.TIME_LIMIT,
                timeLimitDecorator.getTimeLimit()
        ));
        timeLimitDecorator.getWrappedNode().accept(this);
    }

    @Override
    public void visit(ProgressionDecorator progressionDecorator) {
        accumulatedDecorators.add(new FlowDecoratorBean(
                progressionDecorator.getId(),
                FlowDecoratorType.PROGRESSION,
                progressionDecorator.getProgressionString()
        ));
        progressionDecorator.getWrappedNode().accept(this);
    }

    @Override
    public void visit(IntervalDecorator intervalDecorator) {
        accumulatedDecorators.add(new FlowDecoratorBean(
                intervalDecorator.getId(),
                FlowDecoratorType.INTERVAL,
                intervalDecorator.getIntervalDuration()
        ));
        intervalDecorator.getWrappedNode().accept(this);
    }
}
