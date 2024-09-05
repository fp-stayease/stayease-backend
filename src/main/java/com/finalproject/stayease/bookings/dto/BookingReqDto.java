package com.finalproject.stayease.bookings.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingReqDto {
    private Double price;
    private BookingItemReqDto bookingItem;
    private BookingRequestReqDto bookingRequest;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;
    private int totalAdults;
    private int totalChildren;
    private int totalInfants;
}
