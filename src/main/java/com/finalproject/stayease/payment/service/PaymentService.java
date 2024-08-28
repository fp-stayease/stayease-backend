package com.finalproject.stayease.payment.service;

import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.payment.entity.Payment;

import java.util.UUID;

public interface PaymentService {
    Payment createPayment(Double amount, String paymentMethod, Booking booking, String paymentStatus);
    Payment createPayment(Double amount, String paymentMethod, Booking booking, String paymentStatus, String bankVa);
    Payment uploadPaymentProof(String imageUrl, UUID bookingId);
    Payment findPaymentByBookingId(UUID bookingId);
    Payment updatePaymentStatus(Long paymentId, String paymentStatus);
    Payment findPaymentById(Long paymentId);
}
