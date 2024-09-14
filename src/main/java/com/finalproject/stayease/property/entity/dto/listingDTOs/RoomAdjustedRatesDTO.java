package com.finalproject.stayease.property.entity.dto.listingDTOs;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomAdjustedRatesDTO {

  private Long propertyId;
  private Long roomId;
  private String roomName;
  private String imageUrl;
  private Integer roomCapacity;
  private BigDecimal basePrice;
  private BigDecimal adjustedPrice;
  private LocalDate date;

}
