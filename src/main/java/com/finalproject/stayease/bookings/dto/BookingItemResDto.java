package com.finalproject.stayease.bookings.dto;

import com.finalproject.stayease.property.entity.dto.RoomDTO;
import lombok.Data;

import java.time.LocalDate;


@Data
public class BookingItemResDto {
    private RoomDTO room;
    private LocalDate extendingUntil;
}
