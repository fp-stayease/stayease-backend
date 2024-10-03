package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.dto.PropertyCurrentDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateRoomRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.users.entity.Users;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoomService {

  // Room management
  Room createRoom(Long propertyId, CreateRoomRequestDTO requestDTO);
  Room updateRoom(Long propertyId, Long roomId, CreateRoomRequestDTO requestDTO);
  Room getRoom(Long propertyId, Long roomId);
  void deleteRoom(Long propertyId, Long roomId);

  // Property and room deletion
  Set<Room> softDeletePropertyAndRoom(Users tenant, Long propertyId);

  // Room retrieval
  List<Room> getRoomsOfProperty(Long propertyId);
  List<Room> getTenantRooms(Long tenantId);
  List<Room> getRoomsAvailability(Long tenantId);
  Optional<Room> findRoomById(Long roomId);

  // Room availability and rates
  RoomAdjustedRatesDTO getRoomRateAndAvailability(Long roomId, LocalDate date);

  // Property current state
  PropertyCurrentDTO getPropertyCurrent(Long propertyId);

  // Unavailable rooms
  List<Room> getUnavailableRoomsByPropertyIdAndDate(Long propertyId, LocalDate date);
}
