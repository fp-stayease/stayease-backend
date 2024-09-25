package com.finalproject.stayease.property.entity.dto.createRequests;

import java.math.BigDecimal;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@RequiredArgsConstructor
public class SetPropertyRateSettingsDTO {

  private boolean useAutoRates;
  private BigDecimal holidayAdjustmentRate;
  private BigDecimal holidayAdjustmentType;
  private BigDecimal longWeekendAdjustmentRate;
  private BigDecimal longWeekendAdjustmentType;

}
