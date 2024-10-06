package com.finalproject.stayease.payment.entity.dto;

import com.finalproject.stayease.payment.entity.Payment;
import com.finalproject.stayease.payment.entity.PaymentStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class PaymentDTO {
    private Long id;
    private Double amount;
    private String paymentMethod;
    private PaymentStatus paymentStatus;
    private String paymentProof;
    private String bankVa;
    private String bank;
    private Instant paymentExpirationAt;

    public PaymentDTO(Payment payment) {
        this.id = payment.getId();
        this.amount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.paymentStatus = payment.getPaymentStatus();
        this.paymentProof = payment.getPaymentProof();
        this.bankVa = payment.getBankVa();
        this.bank = payment.getBankName();
        this.paymentExpirationAt = payment.getPaymentExpirationAt();
    }
}
