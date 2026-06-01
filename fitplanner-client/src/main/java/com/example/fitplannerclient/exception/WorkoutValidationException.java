package com.example.fitplannerclient.exception;

import java.util.List;

public class WorkoutValidationException extends Exception {

    private final transient List<WorkoutValidationError> validationErrors;

    public WorkoutValidationException(List<WorkoutValidationError> errors) {
        super("Validazione del piano fallita con " + errors.size() + " errori");

        this.validationErrors = errors;
    }

    public List<WorkoutValidationError> getValidationErrors() {
        return validationErrors;
    }

    public record WorkoutValidationError(String message, String nodeId) {}
}
