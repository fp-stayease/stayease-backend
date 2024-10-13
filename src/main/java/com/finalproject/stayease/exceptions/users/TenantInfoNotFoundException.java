package com.finalproject.stayease.exceptions.users;

public class TenantInfoNotFoundException extends RuntimeException {
    public TenantInfoNotFoundException(String message) {
        super(message);
    }
    public TenantInfoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
