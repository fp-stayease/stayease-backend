package com.finalproject.stayease.exceptions.properties;

public class PropertyNotFoundException extends RuntimeException {
    public PropertyNotFoundException(String message) {
        super(message);
    }
    public PropertyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
