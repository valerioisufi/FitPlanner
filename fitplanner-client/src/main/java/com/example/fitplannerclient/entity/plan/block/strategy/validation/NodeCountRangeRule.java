package com.example.fitplannerclient.entity.plan.block.strategy.validation;

import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;

public class NodeCountRangeRule implements ValidationRule {
    private final int min;
    private final int max;

    public NodeCountRangeRule(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public ValidationResult validate(ProtocolBlock block) {
        ValidationResult result = new ValidationResult();
        int count = block.getChildrenCount();

        if (count < min || count > max) {
            String errorMessage = (min == max) 
                ? String.format("Il blocco deve contenere esattamente %d esercizi (attuali: %d).", min, count)
                : String.format("Il blocco deve contenere tra %d e %d esercizi (attuali: %d).", min, max, count);
                
            result.addError(errorMessage, block.getId());
        }

        return result;
    }
}
