package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.InvalidRequestException;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.RoomAvailability;
import com.finalproject.stayease.property.entity.dto.RoomAvailabilityDTO;
import com.finalproject.stayease.property.repository.RoomAvailabilityRepository;
import com.finalproject.stayease.property.service.RoomAvailabilityService;
import com.finalproject.stayease.property.service.RoomService;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Data
@RequiredArgsConstructor
public class RoomAvailabilityServiceImpl implements RoomAvailabilityService {

  private final RoomAvailabilityRepository roomAvailabilityRepository;
  private final RoomService roomService;

  @Override
  public RoomAvailability setUnavailability(Long roomId, LocalDate startDate, LocalDate endDate) {
    // TODO: RoomDoesNotExistException
    Room bookedRoom = roomService.findRoomById(roomId).orElseThrow(() -> new DataNotFoundException("Room does not exist!"));
    RoomAvailability roomAvailability = new RoomAvailability();
    roomAvailability.setRoom(bookedRoom);
    roomAvailability.setStartDate(checkDate(startDate));
    roomAvailability.setEndDate(checkDate(endDate));
    roomAvailability.setIsAvailable(false);
    return roomAvailabilityRepository.save(roomAvailability);
  }

  @Override
  public void removeUnavailability(Long roomId, LocalDate startDate, LocalDate endDate) {
    RoomAvailability roomAvailability = roomAvailabilityRepository.findByRoomIdAndDates(roomId, startDate, endDate)
            .orElseThrow(() -> new DataNotFoundException("Data not exist"));

    roomAvailability.preRemove();

    roomAvailabilityRepository.save(roomAvailability);
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
}
