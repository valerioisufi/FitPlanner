package com.example.fitplannerclient.entity.plan.block;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface GroupNode {
    void addNode(PlanNode node);

    void addNodeAt(int index, PlanNode node);

    void removeNode(PlanNode node);

    void removeNodeAt(int index);

    int getChildrenCount();

    PlanNode getNodeAt(int index);

    void replaceNode(int index, PlanNode newNode);

    int indexOf(PlanNode node);

    List<PlanNode> getChildren();
}
