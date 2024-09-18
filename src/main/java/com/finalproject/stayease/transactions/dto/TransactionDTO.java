package com.finalproject.stayease.transactions.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class TransactionDTO {
    private UUID bookingId;
    private String bookingStatus;
    private String paymentMethod;
    private String paymentStatus;
    private Instant paymentExpiredAt;
}
