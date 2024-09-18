package com.finalproject.stayease.bookings.entity.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class BookingRequestDTO {
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private boolean nonSmoking;
    private String other;
}
