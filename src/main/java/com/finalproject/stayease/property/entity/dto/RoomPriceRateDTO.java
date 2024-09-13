package com.finalproject.stayease.property.entity.dto;

import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoomPriceRateDTO {
  private Long propertyId;
  private String propertyName;
  private Long roomId;
  private String roomName;
  private BigDecimal basePrice;
  private AdjustmentType adjustmentType;
  private BigDecimal adjustmentRate; 
}
