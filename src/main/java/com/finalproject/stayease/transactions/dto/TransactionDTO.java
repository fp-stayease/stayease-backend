package com.finalproject.stayease.transactions.dto;

import com.finalproject.stayease.bookings.entity.BookingStatus;
import com.finalproject.stayease.payment.entity.PaymentStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class TransactionDTO {
    private UUID bookingId;
    private BookingStatus bookingStatus;
    private String paymentMethod;
    private PaymentStatus paymentStatus;
    private Instant paymentExpiredAt;
}
