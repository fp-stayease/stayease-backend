package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.dto.PropertyCurrentDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateRoomRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateRoomRequestDTO;
import com.finalproject.stayease.users.entity.Users;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoomService {

  List<Room> getRoomsOfProperty(Long propertyId);
  List<Room> getUnavailableRoomsByPropertyIdAndDate(Long propertyId, LocalDate date);
  Optional<Room> findRoomById(Long roomId);
  Room createRoom(Long propertyId, CreateRoomRequestDTO requestDTO);
  Room updateRoom(Long propertyId, Long roomId, UpdateRoomRequestDTO requestDTO);
  Room getRoom(Long propertyId, Long roomId);

  void deleteRoom(Long propertyId, Long roomId);
  Set<Room> deletePropertyAndRoom(Users tenant, Long propertyId);

  List<Room> getTenantRooms(Long tenantId);
  List<Room> getRoomsAvailability(Long tenantId);

  RoomAdjustedRatesDTO getRoomRateAndAvailability(Long roomId, LocalDate date);

  PropertyCurrentDTO getPropertyCurrent(Long propertyId);
}
