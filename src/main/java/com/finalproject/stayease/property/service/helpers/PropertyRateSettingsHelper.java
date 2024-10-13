package com.finalproject.stayease.property.service.helpers;

import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import com.finalproject.stayease.property.entity.PropertyRateSetting;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPeakSeasonRateRequestDTO;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.property.service.impl.HolidayService;
import com.finalproject.stayease.property.service.impl.HolidayService.Holiday;
import com.finalproject.stayease.property.service.impl.HolidayService.LongWeekend;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PropertyRateSettingsHelper {

  private final PeakSeasonRateService peakSeasonRateService;
  private final HolidayService holidayService;

  public List<PeakSeasonRate> findAutomaticRatesByPropertyAndDateRange(Long propertyId, LocalDate startDate,
      LocalDate endDate) {
    return peakSeasonRateService.findAutomaticRatesByPropertyAndDateRange(propertyId, startDate, endDate);
  }


  public void handleDeactivation(List<PeakSeasonRate> existingAutoRates) {
    if (existingAutoRates != null && !existingAutoRates.isEmpty()) {
      for (PeakSeasonRate rate : existingAutoRates) {
        peakSeasonRateService.removePeakSeasonRate(rate.getId());
      }
    }
  }

  public void handleAutoRatesApplication(PropertyRateSetting setting, LocalDate startDate,
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
      // If the date is neither a holiday nor a long weekend, deactivate rates
      if (!holidayService.isHoliday(date) && !holidayService.isLongWeekend(date) && existingAutoRatesMap.containsKey(date)) {
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
