package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.properties.DuplicateRoomException;
import com.finalproject.stayease.exceptions.properties.PropertyNotFoundException;
import com.finalproject.stayease.exceptions.properties.RoomNotFoundException;
import com.finalproject.stayease.exceptions.utils.InvalidRequestException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.dto.PropertyCurrentDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateRoomRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import com.finalproject.stayease.property.repository.RoomRepository;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.RoomService;
import com.finalproject.stayease.users.entity.Users;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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


  // Room management

  // Creates a new room for a given property
  @Override
  public Room createRoom(Long propertyId, CreateRoomRequestDTO requestDTO) {
    Property property = checkPropertyAndDuplicateRoom(propertyId, requestDTO.getName());
    return createAndSaveRoom(property, requestDTO);
  }

  // Updates an existing room for a given property
  @Override
  public Room updateRoom(Long propertyId, Long roomId, CreateRoomRequestDTO requestDTO) {
    Room existingRoom = checkRoomBelongsToProperty(propertyId, roomId);
    return updateRoomDetails(existingRoom, requestDTO);
  }

  // Retrieves a room for a given property
  @Override
  public Room getRoom(Long propertyId, Long roomId) {
    return checkRoomBelongsToProperty(propertyId, roomId);
  }

  // Deletes a room for a given property
  @Override
  public void deleteRoom(Long propertyId, Long roomId) {
    Room existingRoom = checkRoomBelongsToProperty(propertyId, roomId);
    softDeleteRoom(existingRoom);
  }

  // Property and room deletion

  // Soft deletes a property and its associated rooms
  @Override
  public Set<Room> softDeletePropertyAndRoom(Users tenant, Long propertyId) {
    Property property = propertyService.deleteProperty(tenant, propertyId);
    return softDeleteRooms(property.getRooms());
  }

  // Room retrieval

  // Retrieves all rooms for a given property
  @Override
  public List<Room> getRoomsOfProperty(Long propertyId) {
    Property property = checkProperty(propertyId);
    List<Room> roomList = roomRepository.findAllByPropertyAndDeletedAtIsNull(property);
    if (roomList.isEmpty()) {
      throw new RoomNotFoundException("No rooms found for property id " + propertyId);
    }
    return roomList;
  }

  // Retrieves all rooms for a given tenant
  @Override
  public List<Room> getTenantRooms(Long tenantId) {
    return roomRepository.findRoomByTenantIdAndDeletedAtIsNull(tenantId);
  }

  // Retrieves room availability for a given tenant
  @Override
  public List<Room> getRoomsAvailability(Long tenantId) {
    return roomRepository.findRoomAvailabilitiesByTenantIdAndDeletedAtIsNull(tenantId);
  }

  // Finds a room by its ID
  @Override
  public Optional<Room> findRoomById(Long roomId) {
    return roomRepository.findByIdAndDeletedAtIsNull(roomId);
  }

  // Room availability and rates

  // Retrieves room rate and availability for a given date
  @Override
  public RoomAdjustedRatesDTO getRoomRateAndAvailability(Long roomId, LocalDate date) {
    RoomPriceRateDTO rateAndAvailability = roomRepository.findRoomRateAndAvailability(roomId, date);
    log.info("Room rate and availability for room {} on date {} is {}", roomId, date, rateAndAvailability);
    BigDecimal adjustedPrice = peakSeasonRateService.applyPeakSeasonRate(rateAndAvailability);
    return createRoomAdjustedRatesDTO(rateAndAvailability, adjustedPrice, date);
  }

  // Property current state

  // Retrieves the current state of a property
  @Override
  public PropertyCurrentDTO getPropertyCurrent(Long id) {
    Property property = propertyService.findPropertyById(id)
        .orElseThrow(() -> new PropertyNotFoundException("Property with this ID does not exist or is deleted"));
    List<Room> availableRooms = getRoomsOfProperty(id);
    return new PropertyCurrentDTO(property, availableRooms);
  }

  // Unavailable rooms

  // Retrieves unavailable rooms for a given property and date
  @Override
  public List<Room> getUnavailableRoomsByPropertyIdAndDate(Long propertyId, LocalDate date) {
    return roomRepository.findUnavailableRoomsByPropertyIdAndDate(propertyId, date);
  }

  // Helper methods

  // Checks if a property exists and if a room with the same name already exists
  private Property checkPropertyAndDuplicateRoom(Long propertyId, String roomName) {
    checkDuplicateRoom(propertyId, roomName);
    return checkProperty(propertyId);
  }

  // Checks if a room with the same name already exists for a given property
  private void checkDuplicateRoom(Long propertyId, String name) {
    List<String> roomNames = roomRepository.findAllRoomNamesByPropertyId(propertyId);
    if (roomNames.contains(name)) {
      throw new DuplicateRoomException("Room with name " + name + " already exists");
    }
  }

  // Checks if a property exists
  private Property checkProperty(Long propertyId) {
    return propertyService.findPropertyById(propertyId)
        .orElseThrow(() -> new PropertyNotFoundException("This property does not exist"));
  }

  // Creates and saves a new room
  private Room createAndSaveRoom(Property property, CreateRoomRequestDTO requestDTO) {
    Room room = new Room();
    room.setProperty(property);
    updateRoomFromDTO(room, requestDTO);
    return roomRepository.save(room);
  }

  // Checks if a room exists
  private Room checkRoom(Long roomId) {
    return roomRepository.findByIdAndDeletedAtIsNull(roomId)
        .orElseThrow(() -> new RoomNotFoundException("This room does not exist"));
  }

  // Updates room details
  private Room updateRoomDetails(Room room, CreateRoomRequestDTO requestDTO) {
    updateRoomFromDTO(room, requestDTO);
    return roomRepository.save(room);
  }

  // Updates room details from a DTO
  private void updateRoomFromDTO(Room room, CreateRoomRequestDTO requestDTO) {
    room.setName(requestDTO.getName());
    room.setDescription(requestDTO.getDescription());
    room.setBasePrice(requestDTO.getBasePrice());
    room.setCapacity(requestDTO.getCapacity());
    room.setImageUrl(requestDTO.getImageUrl());
  }

  // Checks if a room belongs to a given property
  private Room checkRoomBelongsToProperty(Long propertyId, Long roomId) {
    checkProperty(propertyId);
    Room room = checkRoom(roomId);
    if (!Objects.equals(room.getProperty().getId(), propertyId)) {
      throw new InvalidRequestException("Property and room do not match. Please enter a valid property ID and room ID that correlate to each other");
    }
    return room;
  }

  // Soft deletes a room
  private void softDeleteRoom(Room room) {
    room.setDeletedAt(Instant.now());
    roomRepository.save(room);
  }

  // Soft deletes a set of rooms
  private Set<Room> softDeleteRooms(Set<Room> rooms) {
    rooms.forEach(this::softDeleteRoom);
    return rooms;
  }

  // Creates a RoomAdjustedRatesDTO from a RoomPriceRateDTO
  private RoomAdjustedRatesDTO createRoomAdjustedRatesDTO(RoomPriceRateDTO rateAndAvailability, BigDecimal adjustedPrice, LocalDate date) {
    return new RoomAdjustedRatesDTO(
        rateAndAvailability.getPropertyId(),
        rateAndAvailability.getRoomId(),
        rateAndAvailability.getRoomName(),
        rateAndAvailability.getImageUrl(),
        rateAndAvailability.getRoomCapacity(),
        rateAndAvailability.getRoomDescription(),
        rateAndAvailability.getBasePrice(),
        adjustedPrice,
        date,
        rateAndAvailability.getIsAvailable()
    );
  }
}
