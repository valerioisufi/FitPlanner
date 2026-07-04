package com.example.fitplannerclient.entity.plan.block.strategy.validation;

import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;

public class NodeCountRangeRule implements ValidationRule {
    private final int min;
    private final int max; // se = -1 allora non c'è limite superiore

    public NodeCountRangeRule(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public NodeCountRangeRule(int min) {
        this(min, -1);
    }

    @Override
    public ValidationResult validate(ProtocolBlock block) {
        ValidationResult result = new ValidationResult();
        int count = block.getChildrenCount();

        if (count < min || (max != -1 && count > max)) {
            String errorMessage;
            if(min == max)
                errorMessage = String.format("Il blocco deve contenere esattamente %d esercizi (attuali: %d).", min, count);
            else if (max == -1)
                errorMessage = String.format("Il blocco deve contenere almeno %d esercizi (attuali: %d).", min, count);
            else
                errorMessage = String.format("Il blocco deve contenere tra %d e %d esercizi (attuali: %d).", min, max, count);
                
            result.addError(errorMessage, block.getId());
        }

        return result;
    }
}
