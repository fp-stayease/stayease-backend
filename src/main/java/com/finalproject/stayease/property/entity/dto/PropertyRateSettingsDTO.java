package com.finalproject.stayease.property.entity.dto;

import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import com.finalproject.stayease.property.entity.PropertyRateSettings;
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

  public PropertyRateSettingsDTO(PropertyRateSettings propertyRateSettings) {
    this.settingsId = propertyRateSettings.getId();
    this.useAutoRates = propertyRateSettings.getUseAutoRates();
    this.holidayAdjustmentRate = propertyRateSettings.getHolidayAdjustmentRate();
    this.holidayAdjustmentType = propertyRateSettings.getHolidayAdjustmentType();
    this.longWeekendAdjustmentRate = propertyRateSettings.getLongWeekendAdjustmentRate();
    this.longWeekendAdjustmentType = propertyRateSettings.getLongWeekendAdjustmentType();
    this.validFrom = propertyRateSettings.getValidFrom();
    this.propertySummary = new PropertySummary(propertyRateSettings.getProperty().getId(),
        propertyRateSettings.getProperty().getName(), propertyRateSettings.getProperty().getImageUrl());
  }



}
