package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyRateSettings;
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
  public PropertyRateSettings getOrCreatePropertyRateSettings(Long propertyId) {
    // Check if the settings exist, if not create default settings
    return propertyRateSettingsRepository.findByPropertyId(propertyId)
        .orElseGet(() -> createDefaultPropertyRateSettings(propertyId));
  }

  @Override
  public PropertyRateSettings updatePropertyRateSettings(Long propertyId, SetPropertyRateSettingsDTO request) {
    PropertyRateSettings propertyRateSettings = getOrCreatePropertyRateSettings(propertyId);

    log.info("Setting request: {}", request);

    // Update the settings
    propertyRateSettings.setUseAutoRates(request.isUseAutoRates());
    propertyRateSettings.setHolidayAdjustmentRate(request.getHolidayAdjustmentRate());
    propertyRateSettings.setHolidayAdjustmentType(request.getHolidayAdjustmentType());
    propertyRateSettings.setLongWeekendAdjustmentRate(request.getLongWeekendAdjustmentRate());
    propertyRateSettings.setLongWeekendAdjustmentType(request.getLongWeekendAdjustmentType());
    propertyRateSettingsRepository.save(propertyRateSettings);

    // apply the settings
    applySettingsForProperty(propertyId, request);

    return propertyRateSettings;
  }

  @Override
  public void applySettingsForProperty(Long propertyId, SetPropertyRateSettingsDTO request) {
    // initial application, for the next 6 months
    log.info("Initial application of property rate settings for property ID: {}", propertyId);
    LocalDate startDate = LocalDate.now();
    LocalDate endDate = startDate.plusMonths(6);
    applySettingsForProperty(propertyId, request, startDate, endDate);
  }

  @Override
  public void applySettingsForProperty(Long propertyId, SetPropertyRateSettingsDTO request, LocalDate startDate,
      LocalDate endDate) {

    log.info("Request to apply: {}", request);

    // * get existing auto rates
    List<PeakSeasonRate> existingAutoRates = peakSeasonRateService.findAutomaticRatesByPropertyAndDateRange(propertyId,
        startDate,
        endDate);

    // * handle !useAutoRates or deactivation
    if (!request.isUseAutoRates()) {
      log.info("Deactivating auto rates for property ID: {}", propertyId);
      log.info("Existing auto rates: {}", existingAutoRates);
      handleDeactivation(existingAutoRates);
      return;
    }

    // * handle useAutoRates
    handleAutoRatesApplication(propertyId, request, startDate, endDate, existingAutoRates);
  }

  @Override
  public void deactivateAutoRates(Long propertyId) {
    log.info("Deactivating auto rates for property ID: {}", propertyId);
    updatePropertyRateSettings(propertyId, new SetPropertyRateSettingsDTO(false, null, null, null, null));
  }

  private PropertyRateSettings createDefaultPropertyRateSettings(Long propertyId) {
    Property property = propertyService.findPropertyById(propertyId)
        .orElseThrow(() -> new DataNotFoundException("Property not found with id: " + propertyId));

    log.info("Creating default property rate settings for property ID: {}", property.getId());

    PropertyRateSettings propertyRateSettings = new PropertyRateSettings();
    propertyRateSettings.setProperty(property);
    propertyRateSettings.setUseAutoRates(false);
    propertyRateSettings.setHolidayAdjustmentRate(null);
    propertyRateSettings.setHolidayAdjustmentType(null);
    propertyRateSettings.setLongWeekendAdjustmentRate(null);
    propertyRateSettings.setLongWeekendAdjustmentType(null);
    return propertyRateSettingsRepository.save(propertyRateSettings);
  }

  private void handleDeactivation(List<PeakSeasonRate> existingAutoRates) {
    log.info("Deactivating auto rates");
    if (existingAutoRates != null && !existingAutoRates.isEmpty()) {
      for (PeakSeasonRate rate : existingAutoRates) {
        peakSeasonRateService.deletePeakSeasonRate(rate.getId());
      }
    }
  }

  private void handleAutoRatesApplication(Long propertyId, SetPropertyRateSettingsDTO request, LocalDate startDate,
      LocalDate endDate, List<PeakSeasonRate> existingAutoRates) {

    Map<LocalDate, List<PeakSeasonRate>> existingAutoRatesMap = existingAutoRates.stream()
        .collect(Collectors.groupingBy(PeakSeasonRate::getStartDate));

    log.info("Setting up for start date: {} to end date: {}", startDate, endDate);

    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      if (holidayService.isHoliday(date)) {
        // set or update holiday rate
        log.info("Setting holiday rate for property ID: {} on date: {}", propertyId, date);
        setOrUpdateAutomaticRate(propertyId, date, request.getHolidayAdjustmentRate(),
            request.getHolidayAdjustmentType(), "Automatic - Holiday", existingAutoRatesMap.get(date));

      } else if (holidayService.isLongWeekend(date)) {
        // set or update long weekend rate
        log.info("Setting long weekend rate for property ID: {} on date: {}", propertyId, date);
        setOrUpdateAutomaticRate(propertyId, date, request.getLongWeekendAdjustmentRate(),
            request.getLongWeekendAdjustmentType(), "Automatic - Long Weekend", existingAutoRatesMap.get(date));

      } else {
        // else remove existing auto rates (cases where holiday or long weekend is removed)
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
