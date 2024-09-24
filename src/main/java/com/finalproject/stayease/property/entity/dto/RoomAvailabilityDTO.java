package com.finalproject.stayease.property.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.finalproject.stayease.property.entity.RoomAvailability;
import java.time.LocalDate;
import lombok.Data;

@Data
public class RoomAvailabilityDTO {

  private Long id;

  private Long roomId;

  private LocalDate startDate;

  private LocalDate endDate;

  @JsonProperty("isAvailable")
  private boolean isAvailable;

  @JsonProperty("isManual")
  private boolean isManual;


  public RoomAvailabilityDTO(RoomAvailability roomAvailability) {
    this.id = roomAvailability.getId();
    this.roomId = roomAvailability.getRoom().getId();
    this.startDate = roomAvailability.getStartDate();
    this.endDate = roomAvailability.getEndDate();
    this.isAvailable = roomAvailability.getIsAvailable();
    this.isManual = roomAvailability.getIsManual();
  }
}
