package com.finalproject.stayease.bookings.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class BookingRequestReqDto {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private Date checkInTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private Date checkOutTime;
    private boolean nonSmoking;
    private String other;
}
