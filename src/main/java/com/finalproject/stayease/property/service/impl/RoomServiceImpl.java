package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.exceptions.InvalidRequestException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateRoomRequestDTO;
import com.finalproject.stayease.property.repository.RoomRepository;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.RoomService;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
public class RoomServiceImpl implements RoomService {

  private final RoomRepository roomRepository;
  private final PropertyService propertyService;

  @Override
  public Room createRoom(CreateRoomRequestDTO requestDTO) {
    checkDuplicate(requestDTO.getName());
    return toRoomEntity(requestDTO);
  }

  private void checkDuplicate(String name) {
    Optional<Room> checkRoom = roomRepository.findByNameIgnoreCase(name);
    if (checkRoom.isPresent()) {
      // TODO : make new DuplicateRoomException
      throw new DuplicateEntryException("Room with name " + name + " already exists");
    }
  }

  private Room toRoomEntity(CreateRoomRequestDTO requestDTO) {
    Optional<Property> checkProperty = propertyService.findPropertyById(requestDTO.getPropertyId());
    if (checkProperty.isEmpty()) {
      // TODO : make new PropertyNotFoundException
      throw new InvalidRequestException("This property does not exist");
    }

    Room room = new Room();
    room.setProperty(checkProperty.get());
    room.setName(requestDTO.getName());
    room.setDescription(requestDTO.getDescription());
    room.setBasePrice(requestDTO.getBasePrice());
    room.setCapacity(requestDTO.getCapacity());
    roomRepository.save(room);
    return room;
  }
}
