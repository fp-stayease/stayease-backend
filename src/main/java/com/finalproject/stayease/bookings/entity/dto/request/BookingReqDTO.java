package com.finalproject.stayease.bookings.entity.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingReqDTO {
    private BookingItemReqDTO bookingItem;
    private BookingRequestReqDTO bookingRequest;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;
    private int totalAdults;
    private int totalChildren;
    private int totalInfants;
}
