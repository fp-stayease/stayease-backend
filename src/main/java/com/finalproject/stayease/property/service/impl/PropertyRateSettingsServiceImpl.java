package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.properties.PropertyNotFoundException;
import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyRateSetting;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPropertyRateSettingsDTO;
import com.finalproject.stayease.property.repository.PropertyRateSettingsRepository;
import com.finalproject.stayease.property.service.PropertyRateSettingsService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.helpers.PropertyRateSettingsHelper;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
@Slf4j
public class PropertyRateSettingsServiceImpl implements PropertyRateSettingsService {

  private final PropertyRateSettingsRepository propertyRateSettingsRepository;
  private final PropertyService propertyService;
  private final PropertyRateSettingsHelper rateSettingsHelper;

  /**
   * Retrieves existing property rate settings or creates default settings if not found.
   * @param propertyId The ID of the property
   * @return The property rate settings
   */
  @Override
  public PropertyRateSetting getOrCreatePropertyRateSettings(Long propertyId) {
    return propertyRateSettingsRepository.findByPropertyId(propertyId)
        .orElseGet(() -> createDefaultPropertyRateSettings(propertyId));
  }

  /**
   * Updates the property rate settings based on the provided request.
   * @param propertyId The ID of the property
   * @param request The request containing new settings
   * @return The updated property rate settings
   */
  @Override
  public PropertyRateSetting updatePropertyRateSettings(Long propertyId, SetPropertyRateSettingsDTO request) {
    PropertyRateSetting propertyRateSetting = getOrCreatePropertyRateSettings(propertyId);
    log.info("Setting request: {}", request);

    // Update the settings
    propertyRateSetting.setUseAutoRates(request.isUseAutoRates());
    propertyRateSetting.setHolidayAdjustmentRate(request.getHolidayAdjustmentRate());
    propertyRateSetting.setHolidayAdjustmentType(request.getHolidayAdjustmentType());
    propertyRateSetting.setLongWeekendAdjustmentRate(request.getLongWeekendAdjustmentRate());
    propertyRateSetting.setLongWeekendAdjustmentType(request.getLongWeekendAdjustmentType());
    propertyRateSettingsRepository.save(propertyRateSetting);

    // Apply the settings
    applySettingForProperty(propertyRateSetting);

    return propertyRateSetting;
  }

  /**
   * Applies the property rate settings for the next 6 months.
   * @param setting The property rate settings to apply
   */
  @Override
  public void applySettingForProperty(PropertyRateSetting setting) {
    log.info("Initial application of property rate settings for property ID: {}", setting.getProperty().getId());
    LocalDate startDate = LocalDate.now();
    LocalDate endDate = startDate.plusMonths(6);
    applySettingForProperty(setting, startDate, endDate);
  }

  /**
   * Applies the property rate settings for a specific date range.
   * @param setting The property rate settings to apply
   * @param startDate The start date of the range
   * @param endDate The end date of the range
   */
  @Override
  public void applySettingForProperty(PropertyRateSetting setting, LocalDate startDate, LocalDate endDate) {
    log.info("Request to apply: {}", setting);
    Long propertyId = setting.getProperty().getId();

    List<PeakSeasonRate> existingAutoRates = rateSettingsHelper.findAutomaticRatesByPropertyAndDateRange(propertyId, startDate,
        endDate);

    if (!setting.getUseAutoRates()) {
      log.info("Existing auto rates: {} for property ID: {} will be deactivated", existingAutoRates, propertyId);
      rateSettingsHelper.handleDeactivation(existingAutoRates);
      return;
    }

    rateSettingsHelper.handleAutoRatesApplication(setting, startDate, endDate, existingAutoRates);
  }

  /**
   * Deactivates auto rates for a specific property.
   * @param propertyId The ID of the property
   */
  @Override
  public void deactivateAutoRates(Long propertyId) {
    log.info("Deactivating auto rates for property ID: {}", propertyId);
    updatePropertyRateSettings(propertyId, new SetPropertyRateSettingsDTO(false, null, null, null, null));
  }

  private PropertyRateSetting createDefaultPropertyRateSettings(Long propertyId) {
    Property property = propertyService.findPropertyById(propertyId)
        .orElseThrow(() -> new PropertyNotFoundException("Property not found with id: " + propertyId));

    log.info("Creating default property rate settings for property ID: {}", property.getId());

    PropertyRateSetting propertyRateSetting = new PropertyRateSetting();
    propertyRateSetting.setProperty(property);
    propertyRateSetting.setUseAutoRates(false);
    propertyRateSetting.setHolidayAdjustmentRate(null);
    propertyRateSetting.setHolidayAdjustmentType(null);
    propertyRateSetting.setLongWeekendAdjustmentRate(null);
    propertyRateSetting.setLongWeekendAdjustmentType(null);
    return propertyRateSettingsRepository.save(propertyRateSetting);
  }

}

