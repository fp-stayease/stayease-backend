package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.RoomAvailability;
import com.finalproject.stayease.property.entity.dto.RoomWithRoomAvailabilityDTO;
import com.finalproject.stayease.users.entity.Users;
import java.time.LocalDate;
import java.util.List;

public interface RoomAvailabilityService {
  RoomAvailability setUnavailability(Long roomId, LocalDate startDate, LocalDate endDate);
  RoomAvailability setUnavailability(Users tenant, Long roomId, LocalDate startDate, LocalDate endDate);
  void removeUnavailability(Long roomId, LocalDate startDate, LocalDate endDate);
  void removeUnavailability(Users tenant, Long roomId, Long unavailabilityId);

  List<RoomAvailability> getRoomAvailabilityByPropertyId(Long propertyId);
  List<RoomWithRoomAvailabilityDTO> getRoomAvailabilityByTenant(Users tenant);
}
