package com.finalproject.stayease.bookings.dto;

import lombok.Data;

import java.util.Date;

@Data
public class BookingRequestResDto {
    private Date checkInTime;
    private Date checkOutTime;
    private boolean nonSmoking;
    private String other;
}
