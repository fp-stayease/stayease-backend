package com.finalproject.stayease.property.entity.dto;

import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeakSeasonRateDTO {

  private Long rateId;
  private LocalDate startDate;
  private LocalDate endDate;
  private BigDecimal adjustmentRate;
  private AdjustmentType adjustmentType;
  private Instant validFrom;
  private String reason;
  private PropertySummary propertySummary;

  public PeakSeasonRateDTO(PeakSeasonRate peakSeasonRate) {
    this.rateId = peakSeasonRate.getId();
    this.startDate = peakSeasonRate.getStartDate();
    this.endDate = peakSeasonRate.getEndDate();
    this.adjustmentRate = peakSeasonRate.getAdjustmentRate();
    this.adjustmentType = peakSeasonRate.getAdjustmentType();
    this.validFrom = peakSeasonRate.getValidFrom();
    this.reason = peakSeasonRate.getReason();
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
