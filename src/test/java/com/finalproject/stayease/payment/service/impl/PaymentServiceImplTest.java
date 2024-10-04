package com.finalproject.stayease.payment.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.bookings.entity.BookingStatus;
import com.finalproject.stayease.bookings.service.BookingService;
import com.finalproject.stayease.cloudinary.service.CloudinaryService;
import com.finalproject.stayease.exceptions.utils.DataNotFoundException;
import com.finalproject.stayease.payment.entity.Payment;
import com.finalproject.stayease.payment.entity.PaymentStatus;
import com.finalproject.stayease.payment.entity.dto.PaymentDTO;
import com.finalproject.stayease.payment.repository.PaymentRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private PaymentServiceImpl paymentServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPaymentForManualTransfer() {
        Double amount = 2400000.00;
        String paymentMethod = "manual_transfer";
        PaymentStatus paymentStatus = PaymentStatus.PENDING;

        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());

        Payment expectedPayment = new Payment();
        expectedPayment.setAmount(2400000.00);
        expectedPayment.setPaymentMethod("manual_transfer");
        expectedPayment.setPaymentStatus(PaymentStatus.PENDING);
        expectedPayment.setPaymentExpirationAt(Instant.now().plus(1, ChronoUnit.HOURS));
        expectedPayment.setBooking(booking);

        when(paymentRepository.save(any(Payment.class))).thenReturn(expectedPayment);

        Payment createdPayment = paymentServiceImpl.createPayment(amount, paymentMethod, booking, paymentStatus);
        assertEquals(expectedPayment.getPaymentMethod(), createdPayment.getPaymentMethod());
        assertEquals(expectedPayment.getId(), createdPayment.getId());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createPaymentForBankTransfer() {
        Double amount = 2400000.00;
        String paymentMethod = "bank_transfer";
        String bankVa = "9024769247";
        String bankName = "bca";
        PaymentStatus paymentStatus = PaymentStatus.PENDING;

        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());

        Payment expectedPayment = new Payment();
        expectedPayment.setAmount(2400000.00);
        expectedPayment.setPaymentMethod("bank_transfer");
        expectedPayment.setBankVa("9024769247");
        expectedPayment.setPaymentStatus(PaymentStatus.PENDING);
        expectedPayment.setPaymentExpirationAt(Instant.now().plus(30, ChronoUnit.MINUTES));
        expectedPayment.setBooking(booking);

        when(paymentRepository.save(any(Payment.class))).thenReturn(expectedPayment);

        Payment createdPayment = paymentServiceImpl.createPayment(amount, paymentMethod, booking, paymentStatus, bankVa, bankName);
        assertEquals(expectedPayment.getPaymentMethod(), createdPayment.getPaymentMethod());
        assertEquals(expectedPayment.getId(), createdPayment.getId());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void uploadPaymentProof_invalidFileType_throwsException() {
        // Arrange
        UUID bookingId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes()
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            paymentServiceImpl.uploadPaymentProof(file, bookingId);
        });
    }

    @Test
    void uploadPaymentProof_fileTooLarge_throwsException() {
        // Arrange
        UUID bookingId = UUID.randomUUID();
        byte[] largeContent = new byte[1025]; // 1025 bytes, just over the 1024 limit
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", largeContent
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            paymentServiceImpl.uploadPaymentProof(file, bookingId);
        });
    }

    @Test
    void uploadPaymentProof_validFile_success() throws Exception {
        UUID bookingId = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test content".getBytes()
        );
        String cloudinaryUrl = "https://cloudinary.com/test-image";
        Payment payment = new Payment();
        payment.setBooking(new Booking());

        when(cloudinaryService.uploadFile(file, "Payment Proof")).thenReturn(cloudinaryUrl);
        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentDTO updatedPayment = paymentServiceImpl.uploadPaymentProof(file, bookingId);

        assertEquals(cloudinaryUrl, updatedPayment.getPaymentProof());
        assertEquals("waiting for confirmation", updatedPayment.getPaymentStatus());
        verify(bookingService, times(1)).updateBooking(any(), eq(BookingStatus.WAITING_FOR_CONFIRMATION));
    }

    @Test
    void findPaymentByBookingId_paymentExists_success() {
        UUID bookingId = UUID.randomUUID();
        Payment payment = new Payment();

        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.of(payment));

        Payment foundPayment = paymentServiceImpl.findPaymentByBookingId(bookingId);

        assertEquals(payment, foundPayment);
        verify(paymentRepository, times(1)).findByBookingId(bookingId);
    }

    @Test
    void findPaymentByBookingId_paymentNotFound_throwsException() {
        UUID bookingId = UUID.randomUUID();

        when(paymentRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> {
            paymentServiceImpl.findPaymentByBookingId(bookingId);
        });
        verify(paymentRepository, times(1)).findByBookingId(bookingId);
    }

    @Test
    void updatePaymentStatus_validPayment_success() {
        Long paymentId = 1L;
        PaymentStatus newStatus = PaymentStatus.SETTLEMENT;
        Payment payment = new Payment();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        Payment updatedPayment = paymentServiceImpl.updatePaymentStatus(paymentId, newStatus);

        assertEquals(newStatus, updatedPayment.getPaymentStatus());
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void updatePaymentStatus_paymentNotFound_throwsException() {
        Long paymentId = 1L;
        PaymentStatus newStatus = PaymentStatus.SETTLEMENT;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> {
            paymentServiceImpl.updatePaymentStatus(paymentId, newStatus);
        });
        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void findPaymentById_paymentExists_success() {
        Long paymentId = 1L;
        Payment payment = new Payment();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        Payment foundPayment = paymentServiceImpl.findPaymentById(paymentId);

        assertEquals(payment, foundPayment);
        verify(paymentRepository, times(1)).findById(paymentId);
    }

    @Test
    void findPaymentById_paymentNotFound_throwsException() {
        Long paymentId = 1L;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> {
            paymentServiceImpl.findPaymentById(paymentId);
        });
        verify(paymentRepository, times(1)).findById(paymentId);
    }

//    @Test
//    void findExpiredPendingPayment_success() {
//        Instant now = Instant.parse("2024-09-06T15:22:46.646115500Z");
//        Payment expiredPayment = new Payment();
//        expiredPayment.setPaymentStatus("pending");
//        expiredPayment.setPaymentExpirationAt(now.minus(1, ChronoUnit.HOURS));
//
//        when(paymentRepository.findByStatusAndExpirationBefore("pending", now)).thenReturn(List.of(expiredPayment));
//
//        var result = paymentServiceImpl.findExpiredPendingPayment();
//
//        assertEquals(0, result.size());
//        verify(paymentRepository, times(1)).findByStatusAndExpirationBefore("pending", now);
//    }

    @Test
    void tenantRejectPayment_validPayment_success() {
        Long paymentId = 1L;
        Payment payment = new Payment();
        payment.setPaymentExpirationAt(Instant.now());

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        paymentServiceImpl.tenantRejectPayment(paymentId);

        assertNotNull(payment.getPaymentExpirationAt());
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).save(payment);
    }

    @Test
    void tenantRejectPayment_paymentNotFound_throwsException() {
        Long paymentId = 1L;

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        assertThrows(DataNotFoundException.class, () -> {
            paymentServiceImpl.tenantRejectPayment(paymentId);
        });
        verify(paymentRepository, times(1)).findById(paymentId);
    }
}
