package com.finalproject.stayease.bookings.dto;

import lombok.Data;

@Data
public class BookingReqDto {
    private Double price;
    private BookingItemReqDto bookingItem;
    private BookingRequestReqDto bookingRequest;
}
