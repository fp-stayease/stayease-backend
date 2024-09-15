package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateRoomRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateRoomRequestDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomService {

  List<Room> getRoomsOfProperty(Long propertyId);
  List<Room> getUnavailableRoomsByPropertyIdAndDate(Long propertyId, LocalDate date);
  Optional<Room> findRoomById(Long roomId);
  Room createRoom(Long propertyId, CreateRoomRequestDTO requestDTO);
  Room updateRoom(Long propertyId, Long roomId, UpdateRoomRequestDTO requestDTO);
  Room getRoom(Long propertyId, Long roomId);
  void deleteRoom(Long propertyId, Long roomId);

  RoomAdjustedRatesDTO getRoomRateAndAvailability(Long roomId, LocalDate date);
}
