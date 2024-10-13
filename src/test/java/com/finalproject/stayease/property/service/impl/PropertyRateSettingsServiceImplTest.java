package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyRateSetting;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPropertyRateSettingsDTO;
import com.finalproject.stayease.property.repository.PropertyRateSettingsRepository;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.helpers.PropertyRateSettingsHelper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PropertyRateSettingsServiceImplTest {

  @Mock
  private PropertyRateSettingsRepository propertyRateSettingsRepository;

  @Mock
  private PropertyService propertyService;

  @Mock
  private PropertyRateSettingsHelper rateSettingsHelper;

  @InjectMocks
  private PropertyRateSettingsServiceImpl propertyRateSettingsService;

  private Property property;
  private PropertyRateSetting propertyRateSetting;
  private SetPropertyRateSettingsDTO settingsDTO;

  @BeforeEach
  void setUp() {
    property = new Property();
    property.setId(1L);

    propertyRateSetting = new PropertyRateSetting();
    propertyRateSetting.setId(1L);
    propertyRateSetting.setProperty(property);
    propertyRateSetting.setUseAutoRates(true);
    propertyRateSetting.setHolidayAdjustmentRate(BigDecimal.valueOf(10));
    propertyRateSetting.setHolidayAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);
    propertyRateSetting.setLongWeekendAdjustmentRate(BigDecimal.valueOf(15));
    propertyRateSetting.setLongWeekendAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);

    settingsDTO = new SetPropertyRateSettingsDTO(
        true,
        BigDecimal.valueOf(10),
        PeakSeasonRate.AdjustmentType.PERCENTAGE,
        BigDecimal.valueOf(15),
        PeakSeasonRate.AdjustmentType.PERCENTAGE
    );
  }

  @Test
  void getOrCreatePropertyRateSettings_Existing() {
    when(propertyRateSettingsRepository.findByPropertyId(1L)).thenReturn(Optional.of(propertyRateSetting));

    PropertyRateSetting result = propertyRateSettingsService.getOrCreatePropertyRateSettings(1L);

    assertNotNull(result);
    assertEquals(propertyRateSetting, result);
  }

  @Test
  void getOrCreatePropertyRateSettings_New() {
    when(propertyRateSettingsRepository.findByPropertyId(1L)).thenReturn(Optional.empty());
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(propertyRateSettingsRepository.save(any(PropertyRateSetting.class))).thenReturn(propertyRateSetting);

    PropertyRateSetting result = propertyRateSettingsService.getOrCreatePropertyRateSettings(1L);

    assertNotNull(result);
    assertEquals(propertyRateSetting, result);
  }

  @Test
  void updatePropertyRateSettings_Success() {
    when(propertyRateSettingsRepository.findByPropertyId(1L)).thenReturn(Optional.of(propertyRateSetting));
    when(propertyRateSettingsRepository.save(any(PropertyRateSetting.class))).thenReturn(propertyRateSetting);
    doNothing().when(rateSettingsHelper).handleAutoRatesApplication(any(), any(), any(), any());

    PropertyRateSetting result = propertyRateSettingsService.updatePropertyRateSettings(1L, settingsDTO);

    assertNotNull(result);
    assertTrue(result.getUseAutoRates());
    assertEquals(BigDecimal.valueOf(10), result.getHolidayAdjustmentRate());
    assertEquals(PeakSeasonRate.AdjustmentType.PERCENTAGE, result.getHolidayAdjustmentType());
    verify(rateSettingsHelper, times(1)).handleAutoRatesApplication(any(), any(), any(), any());
  }

  @Test
  void applySettingForProperty_Success() {
    doNothing().when(rateSettingsHelper).handleAutoRatesApplication(any(), any(), any(), any());

    assertDoesNotThrow(() -> propertyRateSettingsService.applySettingForProperty(propertyRateSetting));
    verify(rateSettingsHelper, times(1)).handleAutoRatesApplication(any(), any(), any(), any());
  }

  @Test
  void applySettingForProperty_WithDateRange_Success() {
    LocalDate startDate = LocalDate.now();
    LocalDate endDate = startDate.plusMonths(6);
    doNothing().when(rateSettingsHelper).handleAutoRatesApplication(any(), any(), any(), any());

    assertDoesNotThrow(() -> propertyRateSettingsService.applySettingForProperty(propertyRateSetting, startDate, endDate));
    verify(rateSettingsHelper, times(1)).handleAutoRatesApplication(any(), eq(startDate), eq(endDate), any());
  }

  @Test
  void deactivateAutoRates_Success() {
    when(propertyRateSettingsRepository.findByPropertyId(1L)).thenReturn(Optional.of(propertyRateSetting));
    when(propertyRateSettingsRepository.save(any(PropertyRateSetting.class))).thenReturn(propertyRateSetting);
    doNothing().when(rateSettingsHelper).handleDeactivation(any());

    assertDoesNotThrow(() -> propertyRateSettingsService.deactivateAutoRates(1L));
    verify(rateSettingsHelper, times(1)).handleDeactivation(any());
  }
}
