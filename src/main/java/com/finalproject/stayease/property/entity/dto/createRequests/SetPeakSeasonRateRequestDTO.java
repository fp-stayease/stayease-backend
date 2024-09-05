package com.finalproject.stayease.property.entity.dto.createRequests;

import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetPeakSeasonRateRequestDTO {
  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal rateAdjustment;
  private AdjustmentType adjustmentType;
}
