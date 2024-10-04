package com.finalproject.stayease.payment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PaymentStatus {
    PENDING,
    SETTLEMENT,
    EXPIRE,
    CANCEL,
    FAILURE;

    @JsonCreator
    public static PaymentStatus fromString(String value) {
        return valueOf(value.toUpperCase());
    }
}
