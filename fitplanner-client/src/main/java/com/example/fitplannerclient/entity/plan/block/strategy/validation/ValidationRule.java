package com.example.fitplannerclient.entity.plan.block.strategy.validation;

import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;

public interface ValidationRule {
    Boolean validate(ProtocolBlock block);
}
