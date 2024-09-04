package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.dto.CreateRoomRequestDTO;

public interface RoomService {
Room createRoom(CreateRoomRequestDTO requestDTO);
}
