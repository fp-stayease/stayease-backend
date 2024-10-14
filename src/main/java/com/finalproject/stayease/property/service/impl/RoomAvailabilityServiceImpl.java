package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.utils.InvalidDateException;
import com.finalproject.stayease.exceptions.utils.InvalidRequestException;
import com.finalproject.stayease.exceptions.auth.UnauthorizedOperationsException;
import com.finalproject.stayease.exceptions.properties.RoomAvailabilityNotFoundException;
import com.finalproject.stayease.exceptions.properties.RoomNotFoundException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.RoomAvailability;
import com.finalproject.stayease.property.entity.dto.RoomAvailabilityDTO;
import com.finalproject.stayease.property.entity.dto.RoomWithRoomAvailabilityDTO;
import com.finalproject.stayease.property.repository.RoomAvailabilityRepository;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.RoomAvailabilityService;
import com.finalproject.stayease.property.service.RoomService;
import com.finalproject.stayease.users.entity.Users;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Data
@RequiredArgsConstructor
@Slf4j
public class RoomAvailabilityServiceImpl implements RoomAvailabilityService {

  private final RoomAvailabilityRepository roomAvailabilityRepository;
  private final RoomService roomService;
  private final PropertyService propertyService;

  // Set room unavailability for a given room
  @Override
  public RoomAvailability setUnavailability(Long roomId, LocalDate startDate, LocalDate endDate) {
    Room bookedRoom = getRoomById(roomId);
    return createRoomAvailability(bookedRoom, startDate, endDate, false, false);
  }

  // Set room unavailability for a given room by tenant
  @Override
  public RoomAvailability setUnavailability(Users tenant, Long roomId, LocalDate startDate, LocalDate endDate) {
    Room existingRoom = getRoomById(roomId);
    validateTenantOwnership(tenant, existingRoom);
    validateDateRange(roomId, startDate, endDate);
    return createRoomAvailability(existingRoom, startDate, endDate, false, true);
  }

  // Remove room unavailability for a given room
  @Override
  public void removeUnavailability(Long roomId, LocalDate startDate, LocalDate endDate) {
    RoomAvailability roomAvailability = findRoomAvailability(roomId, startDate, endDate);
    softDeleteRoomAvailability(roomAvailability);
  }

  // Remove room unavailability for a given room by tenant
  @Override
  public void removeUnavailability(Users tenant, Long roomId, Long unavailabilityId) {
    RoomAvailability roomAvailability = checkOwnership(tenant, roomId, unavailabilityId);
    softDeleteRoomAvailability(roomAvailability);
  }

  // Get room availability by tenant
  @Override
  public List<RoomWithRoomAvailabilityDTO> getRoomAvailabilityByTenant(Users tenant) {
    List<Property> properties = propertyService.findAllByTenant(tenant);
    List<Room> roomsByTenant = getRoomsByProperties(properties);
    List<RoomAvailability> roomAvailabilitiesByProperty = getRoomAvailabilitiesByProperties(properties);

    return createRoomWithRoomAvailabilityDTOs(roomsByTenant, roomAvailabilitiesByProperty);
  }

  // Remove unavailability by rooms deleted at not null
  @Override
  public void removeUnavailabilityByRoomsDeletedAtNotNull(Users tenant, Long propertyId) {
    checkBookedRoomAvailability(propertyId);
    Set<Room> rooms = roomService.softDeletePropertyAndRoom(tenant, propertyId);
    removeManualUnavailabilityForRooms(rooms);
  }

  // Helper methods

  // Get room by ID
  private Room getRoomById(Long roomId) {
    return roomService.findRoomById(roomId)
        .orElseThrow(() -> new RoomNotFoundException("Room does not exist!"));
  }

  // Validate tenant ownership of the room
  private void validateTenantOwnership(Users tenant, Room room) {
    if (!Objects.equals(room.getProperty().getTenant().getId(), tenant.getId())) {
      throw new UnauthorizedOperationsException("You are not authorized to set unavailability for this room");
    }
  }

  // Validate date range for unavailability
  private void validateDateRange(Long roomId, LocalDate startDate, LocalDate endDate) {
    if (startDate.isAfter(endDate)) {
      throw new InvalidDateException("Start date cannot be after end date");
    }
    if (startDate.isBefore(LocalDate.now())) {
      throw new InvalidDateException("Start date cannot be in the past");
    }
    if (roomAvailabilityRepository.existsOverlappingAvailability(roomId, startDate, endDate)) {
      throw new InvalidDateException("Room is already unavailable in this date range");
    }
  }

  // Create room availability
  private RoomAvailability createRoomAvailability(Room room, LocalDate startDate, LocalDate endDate, boolean isAvailable, boolean isManual) {
    RoomAvailability roomAvailability = new RoomAvailability();
    roomAvailability.setRoom(room);
    roomAvailability.setStartDate(startDate);
    roomAvailability.setEndDate(endDate.minusDays(1)); // Adjust end date
    roomAvailability.setIsAvailable(isAvailable);
    roomAvailability.setIsManual(isManual);
    return roomAvailabilityRepository.save(roomAvailability);
  }

  // Find room availability by room ID and date range
  private RoomAvailability findRoomAvailability(Long roomId, LocalDate startDate, LocalDate endDate) {
    return roomAvailabilityRepository.findByRoomIdAndDates(roomId, startDate, endDate.minusDays(1))
        .orElseThrow(() -> new RoomAvailabilityNotFoundException("No availability found for this room in this date range"));
  }

  // Soft delete room availability
  private void softDeleteRoomAvailability(RoomAvailability roomAvailability) {
    roomAvailability.preRemove();
    roomAvailabilityRepository.save(roomAvailability);
  }

  // Check ownership of room availability by tenant
  private RoomAvailability checkOwnership(Users tenant, Long roomId, Long unavailabilityId) {
    RoomAvailability roomAvailability = roomAvailabilityRepository.findById(unavailabilityId)
        .orElseThrow(() -> new RoomAvailabilityNotFoundException("Data not exist"));

    if (!Objects.equals(roomAvailability.getRoom().getId(), roomId) ||
        !Objects.equals(roomAvailability.getRoom().getProperty().getTenant().getId(), tenant.getId())) {
      throw new UnauthorizedOperationsException("You are not authorized to remove this unavailability");
    }

    return roomAvailability;
  }

  // Get rooms by properties
  private List<Room> getRoomsByProperties(List<Property> properties) {
    return properties.stream()
        .flatMap(property -> roomService.getRoomsOfProperty(property.getId()).stream())
        .collect(Collectors.toList());
  }

  // Get room availabilities by properties
  private List<RoomAvailability> getRoomAvailabilitiesByProperties(List<Property> properties) {
    return properties.stream()
        .flatMap(property -> roomAvailabilityRepository.findAllByPropertyId(property.getId()).stream())
        .collect(Collectors.toList());
  }

  // Create RoomWithRoomAvailabilityDTOs
  private List<RoomWithRoomAvailabilityDTO> createRoomWithRoomAvailabilityDTOs(List<Room> rooms, List<RoomAvailability> roomAvailabilities) {
    return rooms.stream()
        .map(room -> {
          List<RoomAvailabilityDTO> roomAvailabilityDTOs = roomAvailabilities.stream()
              .filter(availability -> Objects.equals(availability.getRoom().getId(), room.getId()))
              .map(RoomAvailabilityDTO::new)
              .collect(Collectors.toList());
          return roomAvailabilityDTOs.isEmpty() ? null : new RoomWithRoomAvailabilityDTO(room, roomAvailabilityDTOs);
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  // Check booked room availability
  private void checkBookedRoomAvailability(Long propertyId) {
    List<RoomAvailability> bookedRoomAvailabilities = roomAvailabilityRepository.findAllByPropertyIdAndIsManualFalse(propertyId);
    if (!bookedRoomAvailabilities.isEmpty()) {
      throw new InvalidRequestException("Cannot delete property with booked rooms, please resolve with customer first");
    }
  }

  // Remove manual unavailability for rooms
  private void removeManualUnavailabilityForRooms(Set<Room> rooms) {
    rooms.forEach(room -> {
      Set<RoomAvailability> manualRoomAvailabilities = room.getRoomAvailabilities().stream()
          .filter(RoomAvailability::getIsManual)
          .collect(Collectors.toSet());
      manualRoomAvailabilities.forEach(this::softDeleteRoomAvailability);
    });
  }

}
