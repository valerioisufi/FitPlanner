package com.example.fitplannerclient.exception;

public class RequestException extends RuntimeException {
    private final int statusCode;

    public RequestException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public RequestException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }

    public int getStatusCode() {
        return statusCode;
    }
}