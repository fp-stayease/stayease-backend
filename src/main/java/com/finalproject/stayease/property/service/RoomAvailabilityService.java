package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.RoomAvailability;
import java.time.LocalDate;

public interface RoomAvailabilityService {
  RoomAvailability setUnavailability(Long roomId, LocalDate startDate, LocalDate endDate);
}
