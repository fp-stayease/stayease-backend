package com.finalproject.stayease.payment.service.impl;

import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.payment.entity.Payment;
import com.finalproject.stayease.payment.repository.PaymentRepository;
import com.finalproject.stayease.payment.service.PaymentService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment createPayment(Double amount, String paymentMethod, Booking booking, String paymentStatus) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(amount);
        payment.setPaymentExpirationAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        payment.setPaymentStatus(paymentStatus);
        return paymentRepository.save(payment);
    }

    @Override
    public Payment uploadPaymentProof(String imageUrl, UUID bookingId) {
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new DataNotFoundException("Booking not found"));

        payment.setPaymentProof(imageUrl);
        payment.setPaymentStatus("Waiting for confirmation");
        return paymentRepository.save(payment);
    }
}
