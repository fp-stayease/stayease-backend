package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.exceptions.InvalidRequestException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateRoomRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateRoomRequestDTO;
import com.finalproject.stayease.property.repository.RoomRepository;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.RoomService;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
@Slf4j
public class RoomServiceImpl implements RoomService {

  private final RoomRepository roomRepository;
  private final PropertyService propertyService;
  private final PeakSeasonRateService peakSeasonRateService;


  @Override
  public List<Room> getRoomsOfProperty(Long propertyId) {
    Property property = checkProperty(propertyId);
    List<Room> roomList = roomRepository.findAllByPropertyAndDeletedAtIsNull(property);
    if (roomList.isEmpty()) {
      throw new InvalidRequestException("No room found for property id " + propertyId);
    }
    return roomList;
  }

  @Override
  public List<Room> getUnavailableRoomsByPropertyIdAndDate(Long propertyId, LocalDate date) {
    return roomRepository.findUnavailableRoomsByPropertyIdAndDate(propertyId, date);
  }

  @Override
  public Optional<Room> findRoomById(Long roomId) {
    return roomRepository.findByIdAndDeletedAtIsNull(roomId);
  }

  @Override
  public Room createRoom(Long propertyId, CreateRoomRequestDTO requestDTO) {
    Property property = checkDuplicate(propertyId, requestDTO);
    return toRoomEntity(property, requestDTO);
  }

  @Override
  public Room updateRoom(Long propertyId, Long roomId, UpdateRoomRequestDTO requestDTO) {
    checkDuplicateRoom(propertyId, requestDTO.getName());
    Room existingRoom = checkBelongsToProperty(propertyId, roomId);
    return update(existingRoom, requestDTO);
  }

  @Override
  public Room getRoom(Long propertyId, Long roomId) {
    return checkBelongsToProperty(propertyId, roomId);
  }

  @Override
  public void deleteRoom(Long propertyId, Long roomId) {
    Room existingRoom = checkBelongsToProperty(propertyId, roomId);
    existingRoom.setDeletedAt(Instant.now());
    roomRepository.save(existingRoom);
  }

  @Override
  public List<Room> getTenantRooms(Long tenantId) {
    return roomRepository.findRoomByTenantIdAndDeletedAtIsNull(tenantId);
  }

  @Override
  public List<Room> getRoomsAvailability(Long tenantId) {
    return roomRepository.findRoomAvailabilitiesByTenantIdAndDeletedAtIsNull(tenantId);
  }

  @Override
  public RoomAdjustedRatesDTO getRoomRateAndAvailability(Long roomId, LocalDate date) {
    RoomPriceRateDTO rateAndAvailability = roomRepository.findRoomRateAndAvailability(roomId, date);
    log.info("Room rate and availability for room {} on date {} is {}", roomId, date, rateAndAvailability);
    BigDecimal adjustedPrice = peakSeasonRateService.applyPeakSeasonRate(rateAndAvailability);
    return new RoomAdjustedRatesDTO(rateAndAvailability.getPropertyId(),
        rateAndAvailability.getRoomId(),
        rateAndAvailability.getRoomName(),
        rateAndAvailability.getImageUrl(),
        rateAndAvailability.getRoomCapacity(),
        rateAndAvailability.getRoomDescription(),
        rateAndAvailability.getBasePrice(),
        adjustedPrice, date,
        rateAndAvailability.getIsAvailable());
  }

  private Property checkDuplicate(Long propertyId, CreateRoomRequestDTO requestDTO) {
    checkDuplicateRoom(propertyId, requestDTO.getName());
    return checkProperty(propertyId);
  }

  private void checkDuplicateRoom(Long PropertyId, String name) {
    List<String> roomList = roomRepository.findAllRoomNamesByPropertyId(PropertyId);
    if (roomList.contains(name)) {
      // TODO : make new DuplicateRoomException
      throw new DuplicateEntryException("Room with name " + name + " already exists");
    }
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
    room.setImageUrl(requestDTO.getImageUrl());
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
    Optional.ofNullable(requestDTO.getImageUrl()).ifPresent(room::setImageUrl);
    roomRepository.save(room);
    return room;
  }

  private Room checkBelongsToProperty(Long propertyId, Long roomId) {
    checkProperty(propertyId);
    Room checkRoom = checkRoom(roomId);
    if (!Objects.equals(checkRoom.getProperty().getId(), propertyId)) {
      throw new InvalidRequestException("Property and room does not match! Please enter a valid property ID and room "
                                        + "ID that correlate to each other");
    }
    return checkRoom;
  }
}
