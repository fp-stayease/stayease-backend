package com.finalproject.stayease.payment.service;

import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.payment.entity.dto.PaymentDTO;
import com.finalproject.stayease.payment.entity.Payment;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface PaymentService {
    Payment createPayment(Double amount, String paymentMethod, Booking booking, String paymentStatus);
    Payment createPayment(Double amount, String paymentMethod, Booking booking, String paymentStatus, String bankVa);
    PaymentDTO uploadPaymentProof(MultipartFile file, UUID bookingId) throws IOException;
    Payment findPaymentByBookingId(UUID bookingId);
    Payment updatePaymentStatus(Long paymentId, String paymentStatus);
    Payment findPaymentById(Long paymentId);
    List<Payment> findExpiredPendingPayment();
    void tenantRejectPayment(Long paymentId);
}
