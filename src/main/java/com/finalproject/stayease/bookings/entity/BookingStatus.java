package com.finalproject.stayease.bookings.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum BookingStatus {
    IN_PROGRESS,
    WAITING_FOR_CONFIRMATION,
    PAYMENT_COMPLETE,
    COMPLETED,
    EXPIRED,
    CANCELLED,
    PENDING,
    FAILED;

    @JsonCreator
    public static BookingStatus fromString(String value) {
        return valueOf(value.toUpperCase());
    }
}
