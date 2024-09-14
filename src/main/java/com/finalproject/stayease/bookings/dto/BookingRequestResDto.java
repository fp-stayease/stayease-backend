package com.finalproject.stayease.bookings.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class BookingRequestResDto {
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private boolean nonSmoking;
    private String other;
}
