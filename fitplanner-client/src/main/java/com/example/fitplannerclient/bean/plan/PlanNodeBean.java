package com.example.fitplannerclient.bean.plan;

import java.util.ArrayList;
import java.util.List;

public class PlanNodeBean {
    private String id;
    private String name;
    private NodeType type;

    private List<ExerciseModifierBean> modifiers;
    private List<FlowDecoratorBean> flowDecorators;

    private List<PlanNodeBean> children;

    public PlanNodeBean() {
        this.modifiers = new ArrayList<>();
        this.flowDecorators = new ArrayList<>();
        this.children = new ArrayList<>();
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
}