package com.finalproject.stayease.bookings.entity.dto;

import com.finalproject.stayease.payment.entity.dto.PaymentDTO;
import com.finalproject.stayease.property.entity.dto.PropertyDTO;
import com.finalproject.stayease.users.dto.TenantInfoResDto;
import com.finalproject.stayease.users.dto.UsersResDto;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class BookingDTO {
    private UUID id;
    private UsersResDto user;
    private TenantInfoResDto tenant;
    private PropertyDTO property;
    private Double totalPrice;
    private String status;
    private List<BookingItemDTO> bookingItems;
    private BookingRequestDTO bookingRequest;
    private PaymentDTO payment;
    private Instant createdAt;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int totalAdults;
    private int totalChildren;
    private int totalInfants;
}
