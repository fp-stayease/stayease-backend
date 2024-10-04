package com.finalproject.stayease.exceptions.properties;

public class DuplicateRoomException extends RuntimeException {
    public DuplicateRoomException(String message) {
        super(message);
    }
    public DuplicateRoomException(String message, Throwable cause) {
        super(message, cause);
    }

}
