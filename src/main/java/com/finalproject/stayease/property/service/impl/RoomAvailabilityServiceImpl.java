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

  @Override
  public RoomAvailability setUnavailability(Long roomId, LocalDate startDate, LocalDate endDate) {
    Room bookedRoom = roomService.findRoomById(roomId).orElseThrow(() -> new RoomNotFoundException("Room does not exist!"));
    RoomAvailability roomAvailability = new RoomAvailability();
    roomAvailability.setRoom(bookedRoom);
    roomAvailability.setStartDate(checkDate(startDate));
    roomAvailability.setEndDate(checkDate(endDate.minusDays(1)));
    roomAvailability.setIsAvailable(false);
    return roomAvailabilityRepository.save(roomAvailability);
  }

  @Override
  public RoomAvailability setUnavailability(Users tenant, Long roomId, LocalDate startDate, LocalDate endDate) {
    Room existingRoom = roomService.findRoomById(roomId).orElseThrow(() -> new RoomNotFoundException("Room does not exist!"));
    if (!Objects.equals(existingRoom.getProperty().getTenant().getId(), tenant.getId())) {
      throw new UnauthorizedOperationsException("You are not authorized to set unavailability for this room");
    }
    validateDateRange(roomId, startDate, endDate);
    return setTenantUnavailability(roomId, startDate, endDate);
  }

  @Override
  public void removeUnavailability(Long roomId, LocalDate startDate, LocalDate endDate) {
    RoomAvailability roomAvailability = roomAvailabilityRepository.findByRoomIdAndDates(roomId, startDate, endDate)
            .orElseThrow(() -> new RoomAvailabilityNotFoundException("No availability found for this room in this date range"));

    roomAvailability.preRemove();

    roomAvailabilityRepository.save(roomAvailability);
  }

  @Override
  public void removeUnavailability(Users tenant, Long roomId, Long unavailabilityId) {
    RoomAvailability roomAvailability = checkOwnership(tenant, roomId, unavailabilityId);
    roomAvailability.preRemove();
    roomAvailabilityRepository.save(roomAvailability);
  }

  @Override
 public List<RoomWithRoomAvailabilityDTO> getRoomAvailabilityByTenant(Users tenant) {
  List<Property> properties = propertyService.findAllByTenant(tenant);
  List<Room> roomsByTenant = properties.stream()
          .map(property -> roomService.getRoomsOfProperty(property.getId()))
          .flatMap(List::stream)
          .toList();
  List<RoomAvailability> roomAvailabilitiesByProperty = properties.stream()
          .map(property -> roomAvailabilityRepository.findAllByPropertyId(property.getId()))
          .flatMap(List::stream)
          .toList();
    return roomsByTenant.stream()
          .map(room -> {
            List<RoomAvailabilityDTO> roomAvailabilityDTOs = roomAvailabilitiesByProperty.stream()
                    .filter(roomAvailability -> Objects.equals(roomAvailability.getRoom().getId(), room.getId()))
                    .map(RoomAvailabilityDTO::new)
                    .toList();
            return roomAvailabilityDTOs.isEmpty() ? null : new RoomWithRoomAvailabilityDTO(room, roomAvailabilityDTOs);
          })
          .filter(Objects::nonNull)
          .toList();
}

  @Override
  public void removeUnavailabilityByRoomsDeletedAtNotNull(Users tenant, Long propertyId) {
    checkBookedRoomAvailability(propertyId);
    Set<Room> rooms = roomService.deletePropertyAndRoom(tenant, propertyId);
    rooms.forEach(room -> {
      // only remove manual unavailability
      Set<RoomAvailability> roomAvailabilities = room.getRoomAvailabilities().stream()
        .filter(RoomAvailability::getIsManual)
        .collect(Collectors.toSet());
      roomAvailabilities.forEach(roomAvailability -> {
        roomAvailability.preRemove();
        roomAvailabilityRepository.save(roomAvailability);
      });
    });
  }

  private void checkBookedRoomAvailability(Long propertyId) {
    List<RoomAvailability> bookedRoomAvailabilities =
        roomAvailabilityRepository.findAllByPropertyIdAndIsManualFalse(propertyId);
    if (!bookedRoomAvailabilities.isEmpty()) {
      throw new InvalidRequestException("Cannot delete property with booked rooms, please resolve with customer first");
    }
  }

  private LocalDate checkDate(LocalDate date) {
    RoomAvailability roomAvailability = roomAvailabilityRepository.findRoomAvailabilityByDate(date);
    if (roomAvailability != null) {
      throw new InvalidRequestException("Room is already unavailable in this date: " + date + ". Data: " +
                                        new RoomAvailabilityDTO(roomAvailability));
    }
    return date;
  }

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

  private RoomAvailability setTenantUnavailability(Long roomId, LocalDate startDate, LocalDate endDate) {
    RoomAvailability roomAvailability = new RoomAvailability();
    roomAvailability.setRoom(roomService.findRoomById(roomId).orElseThrow(() -> new RoomNotFoundException("Room does not exist!")));
    roomAvailability.setStartDate(startDate);
    roomAvailability.setEndDate(endDate);
    roomAvailability.setIsAvailable(false);
    roomAvailability.setIsManual(true);
    return roomAvailabilityRepository.save(roomAvailability);
  }


  private RoomAvailability checkOwnership(Users tenant, Long roomId, Long unavailabilityId) {
    RoomAvailability roomAvailability = roomAvailabilityRepository.findById(unavailabilityId)
            .orElseThrow(() -> new RoomAvailabilityNotFoundException("Data not exist"));
    if (!Objects.equals(roomAvailability.getRoom().getId(), roomId)) {
      throw new UnauthorizedOperationsException("You are not authorized to remove this unavailability");
    }
    if (!Objects.equals(roomAvailability.getRoom().getProperty().getTenant().getId(), tenant.getId())) {
      throw new UnauthorizedOperationsException("You are not authorized to remove this unavailability");
    }
    return roomAvailability;
  }

}
