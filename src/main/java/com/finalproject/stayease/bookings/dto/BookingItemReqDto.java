package com.finalproject.stayease.bookings.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class BookingItemReqDto {
    private Long roomId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date checkInDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date checkOutDate;
    private Double price;
    private int totalAdults;
    private int totalChildren;
    private int totalInfants;
}
