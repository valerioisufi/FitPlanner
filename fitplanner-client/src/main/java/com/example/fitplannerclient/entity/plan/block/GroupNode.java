package com.example.fitplannerclient.entity.plan.block;

import com.example.fitplannerclient.entity.plan.PlanNode;

import java.util.List;

public abstract class GroupNode extends PlanNode {
    protected List<PlanNode> children;

    public void addNode(PlanNode node){
        children.add(node);
    }

    public void addNodeAt(int index, PlanNode node) {
        children.add(index, node);
    }

    public void removeNode(PlanNode node){
        children.remove(node);
    }

    public void removeNodeAt(int index) {
        children.remove(index);
    }

    public int getChildrenCount() {
        return children.size();
    }

    public PlanNode getNodeAt(int index) {
        return children.get(index);
    }

    public abstract void replaceNode();

}
