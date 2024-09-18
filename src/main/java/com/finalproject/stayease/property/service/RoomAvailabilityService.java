package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.RoomAvailability;
import java.time.LocalDate;
import java.util.List;

public interface RoomAvailabilityService {
  RoomAvailability setUnavailability(Long roomId, LocalDate startDate, LocalDate endDate);
  void removeUnavailability(Long roomId, LocalDate startDate, LocalDate endDate);
}
