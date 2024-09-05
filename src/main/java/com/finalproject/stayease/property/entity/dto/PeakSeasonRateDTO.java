package com.finalproject.stayease.property.entity.dto;

import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeakSeasonRateDTO {

  private Long id;
  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal rateAdjustment;
  private AdjustmentType adjustmentType;
  private PropertySummary propertySummary;

  public PeakSeasonRateDTO(PeakSeasonRate peakSeasonRate) {
    this.id = peakSeasonRate.getId();
    this.startDate = peakSeasonRate.getStartDate();
    this.endDate = peakSeasonRate.getEndDate();
    this.rateAdjustment = peakSeasonRate.getRateAdjustment();
    this.adjustmentType = peakSeasonRate.getAdjustmentType();
    this.propertySummary = new PeakSeasonRateDTO.PropertySummary(peakSeasonRate.getProperty().getId(),
        peakSeasonRate.getProperty().getName());
  }

  @Data
  @AllArgsConstructor
  private static class PropertySummary {

    private Long propertyId;
    private String propertyName;
  }
}
