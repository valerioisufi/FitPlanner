package com.example.fitplannerclient.entity.plan.block;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.visitor.WorkoutPlanVisitor;

import java.util.*;

public abstract class CompositeNode extends PlanNode implements GroupNode {
    private String name;
    private List<PlanNode> children = new ArrayList<>();

    protected CompositeNode(String name) {
        this.name = name;
    }

    @Override
    public Optional<String> getName() {
        return Optional.ofNullable(this.name);
    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract CompositeNodeType getType();
    public Optional<ProtocolType> getProtocolType() {
        return Optional.empty();
    }
    public Map<String, String> getParameters() {
        return Map.of();
    }
    public void setParameter(String key, String value) {
        // implementazione vuota di default
    }

    @Override
    public void addNode(PlanNode node) {
        children.add(node);
    }

    @Override
    public void addNodeAt(int index, PlanNode node) {
        children.add(index, node);
    }

    @Override
    public boolean removeNode(PlanNode node) {
        return children.remove(node);
    }

    @Override
    public PlanNode removeNodeAt(int index) {
        return children.remove(index);
    }

    @Override
    public int getChildrenCount() {
        return children.size();
    }

    @Override
    public PlanNode getNodeAt(int index) {
        return children.get(index);
    }

    @Override
    public PlanNode replaceNode(int index, PlanNode newNode) {
        PlanNode oldNode = children.remove(index);
        children.add(index, newNode);

        return oldNode;
    }

    @Override
    public int indexOf(PlanNode node) {
        return children.indexOf(node);
    }

    @Override
    public Iterator<PlanNode> iterator() {
        return Collections.unmodifiableList(children).iterator();
    }
}
