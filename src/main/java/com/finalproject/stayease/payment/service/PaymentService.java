package com.finalproject.stayease.payment.service;

import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.payment.entity.PaymentStatus;
import com.finalproject.stayease.payment.entity.dto.PaymentDTO;
import com.finalproject.stayease.payment.entity.Payment;
import com.finalproject.stayease.reports.dto.overview.MonthlySalesDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface PaymentService {
    Payment createPayment(Double amount, String paymentMethod, Booking booking, PaymentStatus paymentStatus);
    Payment createPayment(Double amount, String paymentMethod, Booking booking, PaymentStatus paymentStatus, String bankVa, String bank);
    PaymentDTO uploadPaymentProof(MultipartFile file, UUID bookingId) throws IOException;
    Payment findPaymentByBookingId(UUID bookingId);
    Payment updatePaymentStatus(Long paymentId, PaymentStatus paymentStatus);
    Payment findPaymentById(Long paymentId);
    List<Payment> findExpiredPendingPayment();
    void tenantRejectPayment(Long paymentId);
    List<MonthlySalesDTO> getMonthlySalesByTenantId(Long tenantId);
    void deletePaymentProof(Long paymentId);
}
