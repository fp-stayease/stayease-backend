package com.finalproject.stayease.property.entity.dto.createRequests;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SetUnavailabilityDTO {

  private LocalDate startDate;
  private LocalDate endDate;

}
