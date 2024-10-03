package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyRateSetting;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPropertyRateSettingsDTO;
import com.finalproject.stayease.property.repository.PropertyRateSettingsRepository;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.property.service.PropertyService;
import java.math.BigDecimal;
import java.util.Collections;
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
  private PeakSeasonRateService peakSeasonRateService;

  @Mock
  private HolidayService holidayService;

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
    when(holidayService.getHolidaysInDateRange(any(), any())).thenReturn(Collections.emptyList());
    when(holidayService.getLongWeekendsInDateRange(any(), any())).thenReturn(Collections.emptyList());

    PropertyRateSetting result = propertyRateSettingsService.updatePropertyRateSettings(1L, settingsDTO);

    assertNotNull(result);
    assertTrue(result.getUseAutoRates());
    assertEquals(BigDecimal.valueOf(10), result.getHolidayAdjustmentRate());
    assertEquals(PeakSeasonRate.AdjustmentType.PERCENTAGE, result.getHolidayAdjustmentType());
  }

  @Test
  void applySettingForProperty_UseAutoRates() {
    when(peakSeasonRateService.findAutomaticRatesByPropertyAndDateRange(anyLong(), any(), any()))
        .thenReturn(Collections.emptyList());
    when(holidayService.getHolidaysInDateRange(any(), any())).thenReturn(Collections.emptyList());
    when(holidayService.getLongWeekendsInDateRange(any(), any())).thenReturn(Collections.emptyList());

    assertDoesNotThrow(() -> propertyRateSettingsService.applySettingForProperty(propertyRateSetting));
  }

  @Test
  void applySettingForProperty_DoNotUseAutoRates() {
    propertyRateSetting.setUseAutoRates(false);
    when(peakSeasonRateService.findAutomaticRatesByPropertyAndDateRange(anyLong(), any(), any()))
        .thenReturn(Collections.singletonList(new PeakSeasonRate()));
    doNothing().when(peakSeasonRateService).removePeakSeasonRate(any());

    assertDoesNotThrow(() -> propertyRateSettingsService.applySettingForProperty(propertyRateSetting));
    verify(peakSeasonRateService, times(1)).removePeakSeasonRate(any());
  }

  @Test
  void deactivateAutoRates_Success() {
    when(propertyRateSettingsRepository.findByPropertyId(1L)).thenReturn(Optional.of(propertyRateSetting));
    when(propertyRateSettingsRepository.save(any(PropertyRateSetting.class))).thenReturn(propertyRateSetting);
    when(peakSeasonRateService.findAutomaticRatesByPropertyAndDateRange(anyLong(), any(), any()))
        .thenReturn(Collections.singletonList(new PeakSeasonRate()));
    doNothing().when(peakSeasonRateService).removePeakSeasonRate(any());

    assertDoesNotThrow(() -> propertyRateSettingsService.deactivateAutoRates(1L));
    verify(peakSeasonRateService, times(1)).removePeakSeasonRate(any());
  }
}
