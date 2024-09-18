package com.finalproject.stayease.bookings.entity.dto;

import com.finalproject.stayease.property.entity.dto.RoomDTO;
import lombok.Data;

import java.time.LocalDate;


@Data
public class BookingItemDTO {
    private RoomDTO room;
    private LocalDate extendingUntil;
}
