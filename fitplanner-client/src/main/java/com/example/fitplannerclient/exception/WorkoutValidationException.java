package com.example.fitplannerclient.exception;

import java.util.List;

public class WorkoutValidationException extends Exception {

    private final transient List<WorkoutValidationError> validationErrors;

    public WorkoutValidationException(List<WorkoutValidationError> errors) {
        this.validationErrors = errors;
    }

    @Override
    public String getMessage() {
        StringBuilder errorMessage = new StringBuilder()
                .append(validationErrors.size())
                .append(validationErrors.size() == 1 ? " problema di validazione.\n" : " problemi di validazione.\n");

        for (WorkoutValidationError error : validationErrors) {
            errorMessage.append("- ").append(error.message()).append("\n");
        }

        return errorMessage.toString();
    }

    public record WorkoutValidationError(String message, String nodeId) {}
}
