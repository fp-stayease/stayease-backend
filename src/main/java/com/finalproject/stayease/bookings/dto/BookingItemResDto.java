package com.finalproject.stayease.bookings.dto;

import lombok.Data;

import java.time.LocalDate;


@Data
public class BookingItemResDto {
    private Long roomId;
    private LocalDate extendingUntil;
}
