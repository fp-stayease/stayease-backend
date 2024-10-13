package com.finalproject.stayease.exceptions.properties;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
    public CategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
