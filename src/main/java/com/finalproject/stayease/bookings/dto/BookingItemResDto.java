package com.finalproject.stayease.bookings.dto;

import lombok.Data;

import java.util.Date;

@Data
public class BookingItemResDto {
    private Long roomId;
    private Date checkInDate;
    private Date checkOutDate;
    private Double basePrice;
    private int totalAdults;
    private int totalChildren;
    private int totalInfants;
    private boolean isExtending;
}
