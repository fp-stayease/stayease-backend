package com.finalproject.stayease.property.entity.dto.createRequests;

import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public class SetPropertyRateSettingsDTO {

  private boolean useAutoRates;
  private BigDecimal holidayAdjustmentRate;
  private AdjustmentType holidayAdjustmentType;
  private BigDecimal longWeekendAdjustmentRate;
  private AdjustmentType longWeekendAdjustmentType;

}
