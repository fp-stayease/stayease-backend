package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateRoomRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateRoomRequestDTO;

public interface RoomService {

  Room createRoom(CreateRoomRequestDTO requestDTO);
  Room updateRoom(Long roomId, UpdateRoomRequestDTO requestDTO);
  void deleteRoom(Long roomId);
  Room findById(Long roomId);
}
