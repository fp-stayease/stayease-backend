package com.finalproject.stayease.exceptions.properties;

public class DuplicatePropertyException extends RuntimeException {
    public DuplicatePropertyException(String message) {
        super(message);
    }
    public DuplicatePropertyException(String message, Throwable cause) {
        super(message, cause);
    }

}
