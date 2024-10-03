package com.finalproject.stayease.exceptions.auth;

public class UnauthorizedOperationsException extends RuntimeException {
    public UnauthorizedOperationsException(String message) {
        super(message);
    }
    public UnauthorizedOperationsException(String message, Throwable cause) {
        super(message, cause);
    }
}
