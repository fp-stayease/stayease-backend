package com.finalproject.stayease.property.entity.dto;

import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import com.finalproject.stayease.property.entity.PropertyRateSetting;
import com.finalproject.stayease.property.entity.dto.RoomDTO.PropertySummary;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyRateSettingsDTO {

  private Long settingsId;
  private boolean useAutoRates;
  private BigDecimal holidayAdjustmentRate;
  private AdjustmentType holidayAdjustmentType;
  private BigDecimal longWeekendAdjustmentRate;
  private AdjustmentType longWeekendAdjustmentType;
  private Instant validFrom;
  private PropertySummary propertySummary;

  public PropertyRateSettingsDTO(PropertyRateSetting propertyRateSetting) {
    this.settingsId = propertyRateSetting.getId();
    this.useAutoRates = propertyRateSetting.getUseAutoRates();
    this.holidayAdjustmentRate = propertyRateSetting.getHolidayAdjustmentRate();
    this.holidayAdjustmentType = propertyRateSetting.getHolidayAdjustmentType();
    this.longWeekendAdjustmentRate = propertyRateSetting.getLongWeekendAdjustmentRate();
    this.longWeekendAdjustmentType = propertyRateSetting.getLongWeekendAdjustmentType();
    this.validFrom = propertyRateSetting.getValidFrom();
    this.propertySummary = new PropertySummary(propertyRateSetting.getProperty().getId(),
        propertyRateSetting.getProperty().getName(), propertyRateSetting.getProperty().getImageUrl());
  }



}
