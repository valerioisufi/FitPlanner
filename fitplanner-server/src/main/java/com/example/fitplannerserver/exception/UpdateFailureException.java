package com.example.fitplannerserver.exception;

public class UpdateFailureException extends RuntimeException {
    public UpdateFailureException(String message) {
        super(message);
    }
}
