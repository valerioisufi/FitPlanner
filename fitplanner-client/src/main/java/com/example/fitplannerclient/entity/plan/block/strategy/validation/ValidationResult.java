package com.example.fitplannerclient.entity.plan.block.strategy.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private final List<ValidationError> errors = new ArrayList<>();

    public void addError(String message, String nodeId) {
        errors.add(new ValidationError(message, nodeId));
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public record ValidationError(String message, String nodeId) {}
}
