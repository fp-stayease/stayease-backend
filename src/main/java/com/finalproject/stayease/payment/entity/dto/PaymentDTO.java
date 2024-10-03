package com.finalproject.stayease.payment.entity.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class PaymentDTO {
    private Long id;
    private Double amount;
    private String paymentMethod;
    private String paymentStatus;
    private String paymentProof;
    private String bankVa;
    private Instant paymentExpirationAt;
}
