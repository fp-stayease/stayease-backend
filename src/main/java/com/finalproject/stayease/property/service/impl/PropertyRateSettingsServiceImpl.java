package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyRateSetting;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPeakSeasonRateRequestDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPropertyRateSettingsDTO;
import com.finalproject.stayease.property.repository.PropertyRateSettingsRepository;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.property.service.PropertyRateSettingsService;
import com.finalproject.stayease.property.service.PropertyService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Data
@RequiredArgsConstructor
@Slf4j
public class PropertyRateSettingsServiceImpl implements PropertyRateSettingsService {

  private final PropertyRateSettingsRepository propertyRateSettingsRepository;
  private final PropertyService propertyService;
  private final PeakSeasonRateService peakSeasonRateService;
  private final HolidayService holidayService;

  @Override
  public PropertyRateSetting getOrCreatePropertyRateSettings(Long propertyId) {
    // Check if the settings exist, if not create default settings
    return propertyRateSettingsRepository.findByPropertyId(propertyId)
        .orElseGet(() -> createDefaultPropertyRateSettings(propertyId));
  }

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

    // apply the settings
    applySettingForProperty(propertyRateSetting);

    return propertyRateSetting;
  }

  @Override
  public void applySettingForProperty(PropertyRateSetting setting) {
    // initial application, for the next 6 months
    log.info("Initial application of property rate settings for property ID: {}", setting.getProperty().getId());
    LocalDate startDate = LocalDate.now();
    LocalDate endDate = startDate.plusMonths(6);
    applySettingForProperty(setting, startDate, endDate);
  }

  @Override
  public void applySettingForProperty(PropertyRateSetting setting, LocalDate startDate,
      LocalDate endDate) {

    log.info("Request to apply: {}", setting);

    Long propertyId = setting.getProperty().getId();

    // * get existing auto rates
    List<PeakSeasonRate> existingAutoRates = peakSeasonRateService.findAutomaticRatesByPropertyAndDateRange(propertyId,
        startDate,
        endDate);

    // * handle !useAutoRates or deactivation
    if (!setting.getUseAutoRates()) {
      log.info("Existing auto rates: {} for property ID: {} will be deactivated", existingAutoRates, propertyId);
      handleDeactivation(existingAutoRates);
      return;
    }

    // * handle useAutoRates
    handleAutoRatesApplication(setting, startDate, endDate, existingAutoRates);
  }

  @Override
  public void deactivateAutoRates(Long propertyId) {
    log.info("Deactivating auto rates for property ID: {}", propertyId);
    updatePropertyRateSettings(propertyId, new SetPropertyRateSettingsDTO(false, null, null, null, null));
  }

  private PropertyRateSetting createDefaultPropertyRateSettings(Long propertyId) {
    Property property = propertyService.findPropertyById(propertyId)
        .orElseThrow(() -> new DataNotFoundException("Property not found with id: " + propertyId));

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

  private void handleDeactivation(List<PeakSeasonRate> existingAutoRates) {
    if (existingAutoRates != null && !existingAutoRates.isEmpty()) {
      for (PeakSeasonRate rate : existingAutoRates) {
        peakSeasonRateService.removePeakSeasonRate(rate.getId());
      }
    }
  }

  private void handleAutoRatesApplication(PropertyRateSetting setting, LocalDate startDate,
      LocalDate endDate, List<PeakSeasonRate> existingAutoRates) {

    Map<LocalDate, List<PeakSeasonRate>> existingAutoRatesMap = existingAutoRates.stream()
        .collect(Collectors.groupingBy(PeakSeasonRate::getStartDate));

    log.info("Setting up for start date: {} to end date: {}", startDate, endDate);

    Long propertyId = setting.getProperty().getId();

    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

      if (holidayService.isHoliday(date)) {
        // set or update holiday rate
        log.info("Setting holiday rate for property ID: {} on date: {}", propertyId, date);
        setOrUpdateAutomaticRate(propertyId, date, setting.getHolidayAdjustmentRate(),
            setting.getHolidayAdjustmentType(), "Automatic - Holiday", existingAutoRatesMap.get(date));

      } else if (holidayService.isLongWeekend(date)) {
        // set or update long weekend rate
        log.info("Setting long weekend rate for property ID: {} on date: {}", propertyId, date);
        setOrUpdateAutomaticRate(propertyId, date, setting.getLongWeekendAdjustmentRate(),
            setting.getLongWeekendAdjustmentType(), "Automatic - Long Weekend", existingAutoRatesMap.get(date));

      } else if (existingAutoRatesMap.containsKey(date)) {
        // else remove existing auto rates (cases where holiday or long weekend is removed)
        log.info("Not a holiday or long weekend, removing existing auto rates for property ID: {} on date: {}",
            propertyId, date);
        handleDeactivation(existingAutoRatesMap.get(date));
      }
    }
  }

  private void setOrUpdateAutomaticRate(Long propertyId, LocalDate date, BigDecimal adjustmentRate,
      AdjustmentType adjustmentType, String reason, List<PeakSeasonRate> existingAutoRates) {

    if (existingAutoRates == null || existingAutoRates.isEmpty()) {
      // no existing rate, create new
      peakSeasonRateService.setPeakSeasonRate(propertyId, new SetPeakSeasonRateRequestDTO(date, date, adjustmentRate,
          adjustmentType, reason));
    } else {
      // update existing rate
      PeakSeasonRate existingRate = existingAutoRates.stream()
          .filter(rate -> rate.getReason().equals(reason))
          .findFirst()
          .orElse(null);

      if (existingRate == null) {
        // create new rate if not found
        peakSeasonRateService.setPeakSeasonRate(propertyId, new SetPeakSeasonRateRequestDTO(date, date, adjustmentRate,
            adjustmentType, reason));

      } else if (!existingRate.getAdjustmentRate().equals(adjustmentRate)
                 || !existingRate.getAdjustmentType().equals(adjustmentType)) {
        // update if rate or type is changed
        peakSeasonRateService.updatePeakSeasonRate(existingRate, adjustmentRate, adjustmentType);
      }
    }

  }
}
