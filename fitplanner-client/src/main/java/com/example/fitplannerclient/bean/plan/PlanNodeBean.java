package com.example.fitplannerclient.bean.plan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanNodeBean {
    private String id;
    private String name;
    private NodeType type;
    private String resourceId; // solo per type == EXERCISE

    private List<ExerciseModifierBean> modifiers;
    private List<FlowDecoratorBean> flowDecorators;

    private List<PlanNodeBean> children;
    private Map<String, String> parameters;

    private String validationErrorMsg; // se null allora nessun errore

    public PlanNodeBean() {
        this.modifiers = new ArrayList<>();
        this.flowDecorators = new ArrayList<>();
        this.children = new ArrayList<>();
        this.parameters = new HashMap<>();
    }

    public PlanNodeBean(String id, String name, NodeType type) {
        this();
        this.id = id;
        this.name = name;
        this.type = type;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public List<PlanNodeBean> getChildren() {
        return children;
    }

    public void setChildren(List<PlanNodeBean> children) {
        this.children = children;
    }

    public void addChild(PlanNodeBean child) {
        if (child != null) this.children.add(child);
    }

    public List<ExerciseModifierBean> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<ExerciseModifierBean> modifiers) {
        this.modifiers = modifiers;
    }

    public void addModifier(ExerciseModifierBean modifier) {
        if (modifier != null) this.modifiers.add(modifier);
    }

    public void addModifiers(List<ExerciseModifierBean> modifiers) {
        if (modifiers != null) this.modifiers.addAll(modifiers);
    }

    public List<FlowDecoratorBean> getFlowDecorators() {
        return flowDecorators;
    }

    public void setFlowDecorators(List<FlowDecoratorBean> flowDecorators) {
        this.flowDecorators = flowDecorators;
    }

    public void addFlowDecorator(FlowDecoratorBean flowDecorator) {
        if (flowDecorator != null) this.flowDecorators.add(flowDecorator);
    }

    public void addFlowDecorators(List<FlowDecoratorBean> flowDecorators) {
        if (flowDecorators != null) this.flowDecorators.addAll(flowDecorators);
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public String getValidationErrorMsg() {
        return validationErrorMsg;
    }

    public void setValidationErrorMsg(String validationErrorMsg) {
        this.validationErrorMsg = validationErrorMsg;
    }
}