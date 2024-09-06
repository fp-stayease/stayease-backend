package com.finalproject.stayease.property.entity.dto;

import com.finalproject.stayease.property.entity.RoomAvailability;
import java.time.LocalDate;
import lombok.Data;

@Data
public class RoomAvailabilityDTO {
  private LocalDate startDate;
  private LocalDate endDate;
  private boolean isAvailable;

  public RoomAvailabilityDTO(RoomAvailability roomAvailability) {
    this.startDate = roomAvailability.getStartDate();
    this.endDate = roomAvailability.getEndDate();
    this.isAvailable = roomAvailability.getIsAvailable();
  }
}
