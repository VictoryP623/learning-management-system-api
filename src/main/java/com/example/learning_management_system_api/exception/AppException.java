package com.example.learning_management_system_api.exception;

public class AppException extends RuntimeException {
    private int statusCode;

    public AppException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
