package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.InvalidRequestException;
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
    // TODO: RoomDoesNotExistException
    Room bookedRoom = roomService.findRoomById(roomId).orElseThrow(() -> new DataNotFoundException("Room does not exist!"));
    RoomAvailability roomAvailability = new RoomAvailability();
    roomAvailability.setRoom(bookedRoom);
    roomAvailability.setStartDate(checkDate(startDate));
    roomAvailability.setEndDate(checkDate(endDate.minusDays(1)));
    roomAvailability.setIsAvailable(false);
    return roomAvailabilityRepository.save(roomAvailability);
  }

  @Override
  public RoomAvailability setUnavailability(Users tenant, Long roomId, LocalDate startDate, LocalDate endDate) {
    Room existingRoom = roomService.findRoomById(roomId).orElseThrow(() -> new DataNotFoundException("Room does not exist!"));
    if (existingRoom.getProperty().getTenant().getId() != tenant.getId()) {
      throw new InvalidRequestException("You are not authorized to set unavailability for this room");
    }
    validateDateRange(roomId, startDate, endDate);
    return setTenantUnavailability(roomId, startDate, endDate);
  }

  @Override
  public void removeUnavailability(Long roomId, LocalDate startDate, LocalDate endDate) {
    RoomAvailability roomAvailability = roomAvailabilityRepository.findByRoomIdAndDates(roomId, startDate, endDate)
            .orElseThrow(() -> new DataNotFoundException("Data not exist"));

    roomAvailability.preRemove();

    roomAvailabilityRepository.save(roomAvailability);
  }

  @Override
  public void removeUnavailability(Users tenant, Long roomId, Long unavailabilityId) {
    RoomAvailability roomAvailability = checkOwnership(tenant, roomId, unavailabilityId);
    log.info("Removing unavailability: " + roomAvailability);
    roomAvailability.preRemove();
    roomAvailabilityRepository.save(roomAvailability);
  }

  @Override
  public List<RoomAvailability> getRoomAvailabilityByPropertyId(Long propertyId) {
    return roomAvailabilityRepository.findAllByPropertyId(propertyId);
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
  List<RoomWithRoomAvailabilityDTO> validRoomAvailability = roomsByTenant.stream()
          .map(room -> {
            List<RoomAvailabilityDTO> roomAvailabilityDTOs = roomAvailabilitiesByProperty.stream()
                    .filter(roomAvailability -> Objects.equals(roomAvailability.getRoom().getId(), room.getId()))
                    .map(RoomAvailabilityDTO::new)
                    .toList();
            return roomAvailabilityDTOs.isEmpty() ? null : new RoomWithRoomAvailabilityDTO(room, roomAvailabilityDTOs);
          })
          .filter(Objects::nonNull)
          .toList();
  log.info("Checking room availability for tenant: " + tenant.getTenantInfo().getBusinessName());
  return validRoomAvailability;
}

  private LocalDate checkDate(LocalDate date) {
    RoomAvailability roomAvailability = roomAvailabilityRepository.findRoomAvailabilityByDate(date);
    if (roomAvailability != null) {
      // TODO : RoomUnavailableException
      throw new InvalidRequestException("Room is already unavailable in this date: " + date + ". Data: " +
                                        new RoomAvailabilityDTO(roomAvailability));
    }
    return date;
  }

  private void validateDateRange(Long roomId, LocalDate startDate, LocalDate endDate) {
    if (startDate.isAfter(endDate)) {
      throw new InvalidRequestException("Start date cannot be after end date");
    }
    if (startDate.isBefore(LocalDate.now())) {
      throw new InvalidRequestException("Start date cannot be in the past");
    }
    if (roomAvailabilityRepository.existsOverlappingAvailability(roomId, startDate, endDate)) {
      throw new InvalidRequestException("Room is already unavailable in this date range");
    }
  }

  private RoomAvailability setTenantUnavailability(Long roomId, LocalDate startDate, LocalDate endDate) {
    RoomAvailability roomAvailability = new RoomAvailability();
    roomAvailability.setRoom(roomService.findRoomById(roomId).orElseThrow(() -> new DataNotFoundException("Room does not exist!")));
    roomAvailability.setStartDate(startDate);
    roomAvailability.setEndDate(endDate);
    roomAvailability.setIsAvailable(false);
    roomAvailability.setIsManual(true);
    return roomAvailabilityRepository.save(roomAvailability);
  }

  private void checkOwnership(Users tenant, Long roomId) {
    Room room = roomService.findRoomById(roomId).orElseThrow(() -> new DataNotFoundException("Room does not exist!"));
    if (!Objects.equals(room.getProperty().getTenant().getId(), tenant.getId())) {
      throw new InvalidRequestException("You are not authorized to set unavailability for this room");
    }
  }

  private RoomAvailability checkOwnership(Users tenant, Long roomId, Long unavailabilityId) {
    RoomAvailability roomAvailability = roomAvailabilityRepository.findById(unavailabilityId)
            .orElseThrow(() -> new DataNotFoundException("Data not exist"));
    if (!Objects.equals(roomAvailability.getRoom().getId(), roomId)) {
      throw new InvalidRequestException("You are not authorized to remove this unavailability");
    }
    if (roomAvailability.getRoom().getProperty().getTenant().getId() != tenant.getId()) {
      throw new InvalidRequestException("You are not authorized to remove this unavailability");
    }
    return roomAvailability;
  }

}
