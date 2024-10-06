package com.finalproject.stayease.bookings.entity.dto;

import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.bookings.entity.BookingStatus;
import com.finalproject.stayease.payment.entity.dto.PaymentDTO;
import com.finalproject.stayease.property.entity.dto.PropertyDTO;
import com.finalproject.stayease.users.entity.dto.TenantInfoDTO;
import com.finalproject.stayease.users.entity.dto.UsersProfileDTO;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class BookingDTO {
    private UUID id;
    private UsersProfileDTO user;
    private TenantInfoDTO tenant;
    private PropertyDTO property;
    private Double totalBasePrice;
    private Double totalPrice;
    private Double serviceFee;
    private Double taxFee;
    private BookingStatus status;
    private List<BookingItemDTO> bookingItems;
    private BookingRequestDTO bookingRequest;
    private PaymentDTO payment;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int totalAdults;
    private int totalChildren;
    private int totalInfants;
    private Instant createdAt;

    public BookingDTO(Booking booking) {
        this.id = booking.getId();
        this.user = new UsersProfileDTO(booking.getUser());
        this.tenant = new TenantInfoDTO(booking.getTenant());
        this.property = new PropertyDTO(booking.getProperty());
        this.totalBasePrice = booking.getTotalBasePrice();
        this.totalPrice = booking.getTotalPrice();
        this.status = booking.getStatus();
        this.bookingItems = booking.getBookingItems().stream().map(BookingItemDTO::new).toList();
        this.bookingRequest = new BookingRequestDTO(booking.getBookingRequest());
        this.payment = new PaymentDTO(booking.getPayment());
        this.checkInDate = booking.getCheckInDate();
        this.checkOutDate = booking.getCheckOutDate();
        this.totalAdults = booking.getTotalAdults();
        this.totalChildren = booking.getTotalChildren();
        this.totalInfants = booking.getTotalInfants();
        this.createdAt = booking.getCreatedAt();
    }
}
