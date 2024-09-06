package com.finalproject.stayease.bookings.dto;

import com.finalproject.stayease.payment.dto.PaymentResDto;
import com.finalproject.stayease.users.dto.TenantInfoResDto;
import com.finalproject.stayease.users.dto.UsersResDto;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class BookingResDto {
    private UUID id;
    private UsersResDto user;
    private TenantInfoResDto tenant;
    private Double totalPrice;
    private String status;
    private List<BookingItemResDto> bookingItems;
    private BookingRequestResDto bookingRequest;
    private PaymentResDto payment;
    private Instant createdAt;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
