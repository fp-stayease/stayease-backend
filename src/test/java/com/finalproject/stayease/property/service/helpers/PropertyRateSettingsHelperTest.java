package com.finalproject.stayease.property.service.helpers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyRateSetting;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.property.service.impl.HolidayService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PropertyRateSettingsHelperTest {

  @Mock
  private PeakSeasonRateService peakSeasonRateService;

  @Mock
  private HolidayService holidayService;

  @InjectMocks
  private PropertyRateSettingsHelper propertyRateSettingsHelper;

  private PropertyRateSetting propertyRateSetting;
  private LocalDate startDate;
  private LocalDate endDate;

  @BeforeEach
  void setUp() {
    propertyRateSetting = new PropertyRateSetting();
    propertyRateSetting.setProperty(new Property());
    propertyRateSetting.getProperty().setId(1L);
    propertyRateSetting.setUseAutoRates(true);
    propertyRateSetting.setHolidayAdjustmentRate(BigDecimal.valueOf(10));
    propertyRateSetting.setHolidayAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);
    propertyRateSetting.setLongWeekendAdjustmentRate(BigDecimal.valueOf(15));
    propertyRateSetting.setLongWeekendAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);

    startDate = LocalDate.now();
    endDate = startDate.plusMonths(6);
  }

  @Test
  void findAutomaticRatesByPropertyAndDateRange_Success() {
    when(peakSeasonRateService.findAutomaticRatesByPropertyAndDateRange(1L, startDate, endDate))
        .thenReturn(new ArrayList<>());

    propertyRateSettingsHelper.findAutomaticRatesByPropertyAndDateRange(1L, startDate, endDate);

    verify(peakSeasonRateService, times(1)).findAutomaticRatesByPropertyAndDateRange(1L, startDate, endDate);
  }

  @Test
  void handleDeactivation_Success() {
    List<PeakSeasonRate> rates = new ArrayList<>();
    rates.add(new PeakSeasonRate());
    rates.getFirst().setId(1L);

    propertyRateSettingsHelper.handleDeactivation(rates);

    verify(peakSeasonRateService, times(1)).removePeakSeasonRate(1L);
  }

  @Test
  void handleAutoRatesApplication_Success() {
    List<PeakSeasonRate> existingRates = new ArrayList<>();
    when(holidayService.getHolidaysInDateRange(startDate, endDate)).thenReturn(new ArrayList<>());
    when(holidayService.getLongWeekendsInDateRange(startDate, endDate)).thenReturn(new ArrayList<>());

    propertyRateSettingsHelper.handleAutoRatesApplication(propertyRateSetting, startDate, endDate, existingRates);

    verify(holidayService, times(1)).getHolidaysInDateRange(startDate, endDate);
    verify(holidayService, times(1)).getLongWeekendsInDateRange(startDate, endDate);
  }
}