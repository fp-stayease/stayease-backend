package com.finalproject.stayease.property.entity.dto.listingDTOs;

import com.finalproject.stayease.property.entity.RoomAvailability;
import java.time.LocalDate;
import lombok.Data;

@Data
public class RoomAvailabilityDTO {
  private Long id;
  private LocalDate startDate;
  private LocalDate endDate;
  private boolean isAvailable;

  public RoomAvailabilityDTO(RoomAvailability roomAvailability) {
    this.id = roomAvailability.getId();
    this.startDate = roomAvailability.getStartDate();
    this.endDate = roomAvailability.getEndDate();
    this.isAvailable = roomAvailability.getIsAvailable();
  }
}
