package com.finalproject.stayease.property.entity.dto.listingDTOs;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyPriceDTO {

  private LocalDate date;
  private BigDecimal lowestPrice;
  private boolean hasAdjustment;

}
