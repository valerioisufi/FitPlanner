package com.example.fitplannerclient.serializer;

import java.util.ArrayList;
import java.util.List;

public class PlanNodeDTO {
    private NodeType type;

    private List<PlanNodeDTO> children = new ArrayList<>();

    private String name;

    private String resourceId; // solo per type == EXERCISE
    private List<Modifier> modifiers = new ArrayList<>();

    private FlowDecorator flowDecorator; // solo per type == FLOW_DECORATOR

    public enum NodeType {
        EXERCISE,
        BLOCK,
        PROTOCOL_BLOCK,
        FLOW_DECORATOR
    }

    public enum FlowDecoratorType {
        LOOP,
        REST,
        TIME_LIMIT,
        INTERVAL,
        PROGRESSION
    }

    public enum ModifierType {
        REPS, WEIGHT, TUT, RPE
    }

    public record Modifier(ModifierType type, String value) {
    }

    public record FlowDecorator(
            FlowDecoratorType type,
            String value
    ) {}


    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public List<PlanNodeDTO> getChildren() {
        return children;
    }

    public void setChildren(List<PlanNodeDTO> children) {
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public List<Modifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

    public FlowDecorator getFlowDecorator() {
        return flowDecorator;
    }

    public void setFlowDecorator(FlowDecorator flowDecorator) {
        this.flowDecorator = flowDecorator;
    }
}
