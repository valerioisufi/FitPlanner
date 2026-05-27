package com.example.fitplannerclient.entity.plan.block;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
