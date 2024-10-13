package com.finalproject.stayease.bookings.entity.dto;

import com.finalproject.stayease.bookings.entity.BookingRequest;
import lombok.Data;

import java.time.LocalTime;

@Data
public class BookingRequestDTO {
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private boolean nonSmoking;
    private String other;

    public BookingRequestDTO(BookingRequest bookingRequest) {
        this.checkInTime = bookingRequest.getCheckInTime();
        this.checkOutTime = bookingRequest.getCheckOutTime();
        this.nonSmoking = bookingRequest.isNonSmoking();
        this.other = bookingRequest.getOther();
    }
}
