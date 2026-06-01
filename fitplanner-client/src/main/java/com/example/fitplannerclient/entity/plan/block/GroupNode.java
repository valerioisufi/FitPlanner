package com.example.fitplannerclient.entity.plan.block;

import com.example.fitplannerclient.entity.plan.PlanNode;

public interface GroupNode extends Iterable<PlanNode> {
    void addNode(PlanNode node);

    void addNodeAt(int index, PlanNode node);

    boolean removeNode(PlanNode node);

    PlanNode removeNodeAt(int index);

    PlanNode replaceNode(int index, PlanNode newNode);

    int getChildrenCount();

    PlanNode getNodeAt(int index);

    int indexOf(PlanNode node);
}
