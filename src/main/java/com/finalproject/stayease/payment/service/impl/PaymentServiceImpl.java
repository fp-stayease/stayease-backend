package com.finalproject.stayease.payment.service.impl;

import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.bookings.entity.BookingStatus;
import com.finalproject.stayease.bookings.service.BookingService;
import com.finalproject.stayease.cloudinary.service.CloudinaryService;
import com.finalproject.stayease.exceptions.utils.DataNotFoundException;
import com.finalproject.stayease.payment.entity.PaymentStatus;
import com.finalproject.stayease.payment.entity.dto.PaymentDTO;
import com.finalproject.stayease.payment.entity.Payment;
import com.finalproject.stayease.payment.repository.PaymentRepository;
import com.finalproject.stayease.payment.service.PaymentService;
import com.finalproject.stayease.reports.dto.overview.MonthlySalesDTO;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.service.TenantInfoService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final CloudinaryService cloudinaryService;
    private final BookingService bookingService;
    private final TenantInfoService tenantInfoService;

    public PaymentServiceImpl(PaymentRepository paymentRepository, CloudinaryService cloudinaryService, BookingService bookingService, TenantInfoService tenantInfoService) {
        this.paymentRepository = paymentRepository;
        this.cloudinaryService = cloudinaryService;
        this.bookingService = bookingService;
        this.tenantInfoService = tenantInfoService;
    }

    @Override
    public Payment createPayment(Double amount, String paymentMethod, Booking booking, PaymentStatus paymentStatus) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(amount);
        payment.setPaymentExpirationAt(Instant.now().plus(1, ChronoUnit.HOURS));
        payment.setPaymentStatus(paymentStatus);
        return paymentRepository.save(payment);
    }

    @Override
    public Payment createPayment(Double amount, String paymentMethod, Booking booking, PaymentStatus paymentStatus, String bankVa, String bank) {
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(amount);
        payment.setPaymentExpirationAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        payment.setPaymentStatus(paymentStatus);
        payment.setBankVa(bankVa);
        payment.setBankName(bank);
        return paymentRepository.save(payment);
    }

    @Override
    public PaymentDTO uploadPaymentProof(MultipartFile file, UUID bookingId) throws IOException {
        List<String> allowedImgType = Arrays.asList("image/jpeg", "image/png", "image/jpg");
        if (!allowedImgType.contains(file.getContentType())) {
            throw new IllegalArgumentException("Image must be un JPEG, JPG, or PNG");
        }
        if (file.getSize() > 1024 * 1024) {
            throw new IllegalArgumentException("File size cannot be greater than 1MB");
        }
        String imageUrl = cloudinaryService.uploadFile(file, "Payment Proof");

        Payment payment = findPaymentByBookingId(bookingId);
        payment.setPaymentProof(imageUrl);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        bookingService.updateBooking(payment.getBooking().getId(), BookingStatus.WAITING_FOR_CONFIRMATION);

        return new PaymentDTO(paymentRepository.save(payment));
    }

    @Override
    public Payment findPaymentByBookingId(UUID bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new DataNotFoundException("Payment not found"));
    }

    @Override
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus paymentStatus) {
        Payment payment = findPaymentById(paymentId);
        payment.setPaymentStatus(paymentStatus);
        return paymentRepository.save(payment);
    }

    @Override
    public Payment findPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new DataNotFoundException("Payment not found"));
    }

    @Override
    public List<Payment> findExpiredPendingPayment() {
        var now = Instant.now();
        return paymentRepository.findByStatusAndExpirationBefore(now);
    }

    @Override
    public void tenantRejectPayment(Long paymentId) {
        Payment payment = findPaymentById(paymentId);
        payment.setPaymentExpirationAt(Instant.now().plus(1, ChronoUnit.HOURS));

        paymentRepository.save(payment);
    }

    @Override
    public List<MonthlySalesDTO> getMonthlySalesByTenantId(Long tenantId) {
        TenantInfo tenant = tenantInfoService.findTenantByUserId(tenantId);
        int year = LocalDate.now().getYear();

        List<MonthlySalesDTO> salesReport = paymentRepository.getMonthlySalesReport(tenant.getId(), year);
        List<MonthlySalesDTO> fullReport = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            int finalMonth = month;
            MonthlySalesDTO monthData = salesReport.stream()
                    .filter(dto -> dto.getMonth() == finalMonth)
                    .findFirst()
                    .orElse(new MonthlySalesDTO(month, 0.0));
            fullReport.add(monthData);
        }
        return fullReport;
    }

    @Override
    public void deletePaymentProof(Long paymentId) {
        Payment payment = findPaymentById(paymentId);
        payment.setPaymentProof(null);
        paymentRepository.save(payment);
    }
}
