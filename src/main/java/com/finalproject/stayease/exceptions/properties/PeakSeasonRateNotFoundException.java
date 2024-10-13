package com.finalproject.stayease.exceptions.properties;

public class PeakSeasonRateNotFoundException extends RuntimeException {
    public PeakSeasonRateNotFoundException(String message) {
        super(message);
    }
    public PeakSeasonRateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
