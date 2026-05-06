package com.example.fitplannerclient.entity.plan.block.strategy;

import com.example.fitplannerclient.entity.plan.PlanNode;

public interface CompositionRule {
    PlanNode apply(PlanNode node);
}
