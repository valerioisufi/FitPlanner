package com.example.fitplannerclient.controller.plan.mapper;

import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.bean.plan.FlowDecoratorType;
import com.example.fitplannerclient.entity.plan.block.CompositeNode;
import com.example.fitplannerclient.entity.plan.block.strategy.validation.ValidationResult;
import com.example.fitplannerclient.entity.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import com.example.fitplannerclient.util.IDGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlanToBeanVisitor implements WorkoutPlanVisitor {

    private WorkoutPlanBean planBean;
    private final List<WorkoutSessionBean> sessionBeans = new ArrayList<>();

    private PlanNodeBean currentPlanNodeBean;
    private final List<FlowDecoratorBean> accumulatedDecorators = new ArrayList<>();

    private ValidationResult validationResult;

    public PlanToBeanVisitor() {}

    public PlanToBeanVisitor(ValidationResult validationResult) {
        this.validationResult = validationResult;
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
        String name = exerciseNode.getName().orElse("Esercizio Sconosciuto");
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

        StringBuilder errorMsg = new StringBuilder();
        String selfError = getErrorMessage(exerciseNode.getId());
        if (selfError != null) {
            errorMsg.append(selfError).append("\n");
        }
        for (FlowDecoratorBean dec : accumulatedDecorators) {
            String decError = getErrorMessage(dec.getId());
            if (decError != null) {
                errorMsg.append(decError).append("\n");
            }
        }
        nodeBean.setValidationErrorMsg(errorMsg.isEmpty() ? null : errorMsg.toString().trim());

        accumulatedDecorators.clear();

        currentPlanNodeBean = nodeBean;
    }

    @Override
    public void visit(CompositeNode compositeNode) {
        String name = compositeNode.getName().orElse("Senza nome");
        NodeType nodeType = switch (compositeNode.getType()) {
            case BLOCK ->  NodeType.BLOCK;
            case PROTOCOL ->   NodeType.PROTOCOL;
        };

        PlanNodeBean nodeBean = new PlanNodeBean(compositeNode.getId(), name, nodeType);
        nodeBean.setFlowDecorators(new ArrayList<>(accumulatedDecorators));
        nodeBean.setParameters(compositeNode.getParameters() != null ? new HashMap<>(compositeNode.getParameters()) : new HashMap<>());

        StringBuilder errorMsg = new StringBuilder();
        String selfError = getErrorMessage(compositeNode.getId());
        if (selfError != null) {
            errorMsg.append(selfError).append("\n");
        }
        for (FlowDecoratorBean dec : accumulatedDecorators) {
            String decError = getErrorMessage(dec.getId());
            if (decError != null) {
                errorMsg.append(decError).append("\n");
            }
        }
        nodeBean.setValidationErrorMsg(errorMsg.isEmpty() ? null : errorMsg.toString().trim());

        accumulatedDecorators.clear();

        for (int i = 0; i < compositeNode.getChildrenCount(); i++) {
            PlanNode child = compositeNode.getNodeAt(i);

            // I figli partono con un accumulatore pulito
            accumulatedDecorators.clear();

            child.accept(this);
            nodeBean.addChild(currentPlanNodeBean);
        }

        currentPlanNodeBean = nodeBean;
    }

    @Override
    public void visit(FlowDecorator flowDecorator) {
        accumulatedDecorators.add(new FlowDecoratorBean(
                flowDecorator.getId(),
                FlowDecoratorType.valueOf(flowDecorator.getType().toString()),
                flowDecorator.getSerializedValue()
        ));
        flowDecorator.getWrappedNode().accept(this);
    }

    private String getErrorMessage(String nodeId) {
        if (validationResult == null) return null;

        StringBuilder errorMsg = new StringBuilder();
        for(ValidationResult.ValidationError error : validationResult.getErrorsByNodeId(nodeId)) {
            errorMsg.append(error.message()).append("\n");
        }

        if (errorMsg.isEmpty()) return null;
        return errorMsg.toString();
    }
}
