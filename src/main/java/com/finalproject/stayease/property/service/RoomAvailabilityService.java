package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.RoomAvailability;
import com.finalproject.stayease.property.entity.dto.RoomWithRoomAvailabilityDTO;
import com.finalproject.stayease.users.entity.Users;
import java.time.LocalDate;
import java.util.List;

public interface RoomAvailabilityService {
  // Set room unavailability
  RoomAvailability setUnavailability(Long roomId, LocalDate startDate, LocalDate endDate);
  RoomAvailability setUnavailability(Users tenant, Long roomId, LocalDate startDate, LocalDate endDate);

  // Remove room unavailability
  void removeUnavailability(Long roomId, LocalDate startDate, LocalDate endDate);
  void removeUnavailability(Users tenant, Long roomId, Long unavailabilityId);

  // Get room availability by tenant
  List<RoomWithRoomAvailabilityDTO> getRoomAvailabilityByTenant(Users tenant);

  // Remove unavailability by rooms deleted at not null
  void removeUnavailabilityByRoomsDeletedAtNotNull(Users tenant, Long propertyId);
}
