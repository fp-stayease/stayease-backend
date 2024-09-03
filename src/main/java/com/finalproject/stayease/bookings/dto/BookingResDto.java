package com.finalproject.stayease.bookings.dto;

import com.finalproject.stayease.payment.dto.PaymentResDto;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class BookingResDto {
    private UUID id;
    private Long userId;
    private Long tenantId;
    private Double totalPrice;
    private String status;
    private List<BookingItemResDto> bookingItems;
    private BookingRequestResDto bookingRequest;
    private PaymentResDto payment;
    private Instant createdAt;
}
