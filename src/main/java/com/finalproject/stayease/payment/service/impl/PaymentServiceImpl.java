package com.finalproject.stayease.payment.service.impl;

import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.cloudinary.service.CloudinaryService;
import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.payment.entity.Payment;
import com.finalproject.stayease.payment.repository.PaymentRepository;
import com.finalproject.stayease.payment.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final CloudinaryService cloudinaryService;

    public PaymentServiceImpl(PaymentRepository paymentRepository, CloudinaryService cloudinaryService) {
        this.paymentRepository = paymentRepository;
        this.cloudinaryService = cloudinaryService;
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
    public Payment createPayment(Double amount, String paymentMethod, Booking booking, String paymentStatus, String bankVa) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(amount);
        payment.setPaymentExpirationAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        payment.setPaymentStatus(paymentStatus);
        payment.setBankVa(bankVa);
        return paymentRepository.save(payment);
    }

    @Override
    public Payment uploadPaymentProof(MultipartFile file, UUID bookingId) throws IOException {
        List<String> allowedImgType = Arrays.asList("image/jpeg", "image/png", "image/jpg");
        if (!allowedImgType.contains(file.getContentType())) {
            throw new IllegalArgumentException("Image must be un JPEG, JPG, or PNG");
        }
        String imageUrl = cloudinaryService.uploadFile(file, "Payment Proof");

        Payment payment = findPaymentByBookingId(bookingId);
        payment.setPaymentProof(imageUrl);
        payment.setPaymentStatus("Waiting for confirmation");

        return paymentRepository.save(payment);
    }

    @Override
    public Payment findPaymentByBookingId(UUID bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new DataNotFoundException("Payment not found"));
    }

    @Override
    public Payment updatePaymentStatus(Long paymentId, String paymentStatus) {
        Payment payment = findPaymentById(paymentId);
        payment.setPaymentStatus(paymentStatus);
        return paymentRepository.save(payment);
    }

    @Override
    public Payment findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new DataNotFoundException("Payment not found"));
    }
}
