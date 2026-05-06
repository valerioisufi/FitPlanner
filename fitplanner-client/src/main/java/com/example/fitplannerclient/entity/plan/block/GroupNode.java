package com.example.fitplannerclient.entity.plan.block;

import com.example.fitplannerclient.entity.plan.PlanNode;

import java.util.List;

public abstract class GroupNode extends PlanNode {
    protected List<PlanNode> children;

    public void addNode(PlanNode node){
        children.add(node);
    }
    public void removeNode(PlanNode node){
        children.remove(node);
    }
    public abstract void addNodeAt(int index, PlanNode node);
    public abstract void replaceNode();

}
