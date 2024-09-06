package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.RoomAvailability;
import com.finalproject.stayease.property.repository.RoomAvailabilityRepository;
import com.finalproject.stayease.property.service.RoomAvailabilityService;
import com.finalproject.stayease.property.service.RoomService;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Data
public class RoomAvailabilityServiceImpl implements RoomAvailabilityService {

  private final RoomAvailabilityRepository roomAvailabilityRepository;
  private final RoomService roomService;

  @Override
  public RoomAvailability setUnavailability(Long roomId, LocalDate startDate, LocalDate endDate) {
    // TODO: RoomDoesNotExistException
    Room bookedRoom = roomService.findRoomById(roomId).orElseThrow(() -> new DataNotFoundException("Room does not exist!"));
    RoomAvailability roomAvailability = new RoomAvailability();
    roomAvailability.setRoom(bookedRoom);
    roomAvailability.setStartDate(startDate);
    roomAvailability.setEndDate(endDate);
    roomAvailability.setIsAvailable(false);
    return roomAvailabilityRepository.save(roomAvailability);
  }
}
