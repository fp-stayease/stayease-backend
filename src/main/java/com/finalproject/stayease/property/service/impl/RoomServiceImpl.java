package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.exceptions.InvalidRequestException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateRoomRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateRoomRequestDTO;
import com.finalproject.stayease.property.repository.RoomRepository;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.RoomService;
import jakarta.transaction.Transactional;
import java.time.Instant;
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
    Property property = checkDuplicate(requestDTO);
    return toRoomEntity(property, requestDTO);
  }

  @Override
  public Room updateRoom(Long roomId, UpdateRoomRequestDTO requestDTO) {
    Room existingRoom = checkRoom(roomId);
    return update(existingRoom, requestDTO);
  }

  @Override
  public void deleteRoom(Long roomId) {
    Room existingRoom = checkRoom(roomId);
    existingRoom.setDeletedAt(Instant.now());
    roomRepository.save(existingRoom);
  }

  private Property checkDuplicate(CreateRoomRequestDTO requestDTO) {
    Optional<Room> checkRoom = roomRepository.findByNameIgnoreCaseAndDeletedAtIsNull(requestDTO.getName());
    if (checkRoom.isPresent()) {
      // TODO : make new DuplicateRoomException
      throw new DuplicateEntryException("Room with name " + requestDTO.getName() + " already exists");
    }
    return checkProperty(requestDTO.getPropertyId());
  }

  private Property checkProperty(Long propertyId) {
    Optional<Property> checkProperty = propertyService.findPropertyById(propertyId);
    if (checkProperty.isEmpty()) {
      // TODO : make new PropertyNotFoundException
      throw new InvalidRequestException("This property does not exist");
    }
    return checkProperty.get();
  }

  private Room toRoomEntity(Property property, CreateRoomRequestDTO requestDTO) {
    Room room = new Room();
    room.setProperty(property);
    room.setName(requestDTO.getName());
    room.setDescription(requestDTO.getDescription());
    room.setBasePrice(requestDTO.getBasePrice());
    room.setCapacity(requestDTO.getCapacity());
    roomRepository.save(room);
    return room;
  }

  private Room checkRoom(Long roomId) {
    Optional<Room> checkRoom = roomRepository.findByIdAndDeletedAtIsNull(roomId);
    if (checkRoom.isEmpty()) {
      // TODO : make RoomDoesNotExistException
      throw new InvalidRequestException("This room does not exist");
    }
    return checkRoom.get();
  }

  private Room update(Room room, UpdateRoomRequestDTO requestDTO) {
    Optional.ofNullable(requestDTO.getName()).ifPresent(room::setName);
    Optional.ofNullable(requestDTO.getDescription()).ifPresent(room::setDescription);
    Optional.ofNullable(requestDTO.getBasePrice()).ifPresent(room::setBasePrice);
    Optional.ofNullable(requestDTO.getCapacity()).ifPresent(room::setCapacity);
    roomRepository.save(room);
    return room;
  }
}
