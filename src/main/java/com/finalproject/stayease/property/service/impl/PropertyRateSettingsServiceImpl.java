package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.properties.PropertyNotFoundException;
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
import com.finalproject.stayease.property.service.impl.HolidayService.Holiday;
import com.finalproject.stayease.property.service.impl.HolidayService.LongWeekend;
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

    log.info("Setting request: " + request);

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

    List<Holiday> holidays = holidayService.getHolidaysInDateRange(startDate, endDate);
    List<LongWeekend> longWeekends = holidayService.getLongWeekendsInDateRange(startDate, endDate);

    setHolidaysRate(setting, holidays, existingAutoRatesMap);
    setLongWeekendsRate(setting, longWeekends, existingAutoRatesMap);
    deactivateNonHolidayAndNonLongWeekendRates(setting, startDate, endDate, existingAutoRatesMap);
  }

  private void setHolidaysRate(PropertyRateSetting setting, List<Holiday> holidays, Map<LocalDate,
      List<PeakSeasonRate>> existingAutoRatesMap) {
    Long propertyId = setting.getProperty().getId();
    if (setting.getHolidayAdjustmentRate() != null && setting.getHolidayAdjustmentType() != null) {
      for (Holiday holiday : holidays) {
        LocalDate date = holiday.getDate();
        log.info("Setting holiday rate for property ID: {} on date: {}", propertyId, date);
        setOrUpdateAutomaticRate(propertyId, date, date, setting.getHolidayAdjustmentRate(),
            setting.getHolidayAdjustmentType(), "Automatic - Holiday", existingAutoRatesMap.get(date));
      }
    } else {
      log.info("Skipping holiday rate setting for property ID: {} as rate or type is null", propertyId);
    }
  }

  private void setLongWeekendsRate(PropertyRateSetting setting, List<LongWeekend> longWeekends, Map<LocalDate,
      List<PeakSeasonRate>> existingAutoRatesMap) {
    Long propertyId = setting.getProperty().getId();
    if (setting.getLongWeekendAdjustmentRate() != null && setting.getLongWeekendAdjustmentType() != null) {
      for (LongWeekend longWeekend : longWeekends) {
        LocalDate longWeekendStartDate = longWeekend.getStartDate();
        LocalDate longWeekendEndDate = longWeekend.getEndDate();
        log.info("Setting long weekend rate for property ID: {} on date: {}", propertyId, longWeekendStartDate);
        setOrUpdateAutomaticRate(propertyId, longWeekendStartDate, longWeekendEndDate,
            setting.getLongWeekendAdjustmentRate(),
            setting.getLongWeekendAdjustmentType(), "Automatic - Long Weekend", existingAutoRatesMap.get(
                longWeekendStartDate));
      }
    } else {
      log.info("Skipping long weekend rate setting for property ID: {} as rate or type is null", propertyId);
    }
  }

  private void deactivateNonHolidayAndNonLongWeekendRates(PropertyRateSetting setting, LocalDate startDate,
      LocalDate endDate, Map<LocalDate, List<PeakSeasonRate>> existingAutoRatesMap) {
    Long propertyId = setting.getProperty().getId();
    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      if (existingAutoRatesMap.containsKey(date)) {
        // else remove existing auto rates (cases where holiday or long weekend is removed)
        log.info("Not a holiday or long weekend, removing existing auto rates for property ID: {} on date: {}",
            propertyId, date);
        handleDeactivation(existingAutoRatesMap.get(date));
      }
    }
  }

  private void setOrUpdateAutomaticRate(Long propertyId, LocalDate startDate,
      LocalDate endDate, BigDecimal adjustmentRate,
      AdjustmentType adjustmentType, String reason, List<PeakSeasonRate> existingAutoRates) {
    if (adjustmentRate == null || adjustmentType == null) {
      log.info("Skipping rate setting for property ID: {} as rate or type is null", propertyId);
      return;
    }
    if (existingAutoRates == null || existingAutoRates.isEmpty()) {
      createNewRate(propertyId, new SetPeakSeasonRateRequestDTO(startDate, endDate, adjustmentRate,
          adjustmentType, reason));
    } else {
      PeakSeasonRate existingRate = findExistingRateByReason(existingAutoRates, reason);
      if (existingRate == null) {
        createNewRate(propertyId, new SetPeakSeasonRateRequestDTO(startDate, endDate, adjustmentRate,
            adjustmentType, reason));
      } else if (rateOrTypeChanged(existingRate, adjustmentRate, adjustmentType)) {
        updateExistingRate(existingRate, adjustmentRate, adjustmentType);
      }
    }
  }

  private void createNewRate(Long propertyId, SetPeakSeasonRateRequestDTO requestDTO) {
    peakSeasonRateService.setPeakSeasonRate(propertyId, requestDTO);
  }

  private void updateExistingRate(PeakSeasonRate existingRate, BigDecimal adjustmentRate,
      AdjustmentType adjustmentType) {
    peakSeasonRateService.updatePeakSeasonRate(existingRate, adjustmentRate, adjustmentType);
  }

  private PeakSeasonRate findExistingRateByReason(List<PeakSeasonRate> existingAutoRates, String reason) {
    return existingAutoRates.stream()
        .filter(rate -> rate.getReason().equals(reason))
        .findFirst()
        .orElse(null);
  }

  private boolean rateOrTypeChanged(PeakSeasonRate existingRate, BigDecimal adjustmentRate,
      AdjustmentType adjustmentType) {
    return !existingRate.getAdjustmentRate().equals(adjustmentRate)
        || !existingRate.getAdjustmentType().equals(adjustmentType);
  }

}

