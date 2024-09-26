package com.finalproject.stayease.scheduler;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyRateSetting;
import com.finalproject.stayease.property.service.PropertyRateSettingsService;
import com.finalproject.stayease.property.service.PropertyService;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * * This scheduler's task is to set and update the peak season rate for all properties that have auto rates enabled.
 * The first method runs weekly every Sunday at midnight, and the second method runs daily at midnight.
 * It fetches all properties with auto rates enabled and updates the peak season rate for each property.
 * The purpose of the first method is to ensure that there is always a rate set for the next 6 months.
 * While the second method is to ensure that the rates are updated daily, in case of any changes in the holiday or
 * long weekend dates. It will update the rates for the next 3 days.
 */

@Service
@Data
@Slf4j
@Transactional
public class AutomaticPeakSeasonRateScheduler {

  private final PropertyRateSettingsService propertyRateSettingsService;
  private final PropertyService propertyService;

  @Scheduled(cron = "${cron.auto-rates.weekly:0 0 0 * * SUN}")
  public void weeklyFullAutomaticPeakSeasonRateUpdate() {
    log.info("Updating automatic peak season rate...");

    // Get all properties that have auto rates enabled
    List<Property> propertiesWithAutoRatesEnabled = propertyService.findAllPropertiesWithAutoRatesEnabled();

    // Update the peak season rate for each property
    propertiesWithAutoRatesEnabled.forEach(property -> {
      PropertyRateSetting validRateSettings = propertyRateSettingsService.getOrCreatePropertyRateSettings(property.getId());
      propertyRateSettingsService.applySettingForProperty(validRateSettings);
    });
    log.info("Automatic peak season rate updated");
  }

  @Scheduled(cron = "${cron.auto-rates.incremental: 0 0 0 * * ?}")
  public void incrementalAutomaticPeakSeasonRateUpdate() {
    log.info("Updating incremental automatic peak season rate...");
    LocalDate startDate = LocalDate.now();
    LocalDate endDate = startDate.plusDays(3);

    // Get all properties that have auto rates enabled
    List<Property> propertiesWithAutoRatesEnabled = propertyService.findAllPropertiesWithAutoRatesEnabled();

    // Update the peak season rate for each property
    propertiesWithAutoRatesEnabled.forEach(property -> {
      PropertyRateSetting validRateSettings = propertyRateSettingsService.getOrCreatePropertyRateSettings(property.getId());
      propertyRateSettingsService.applySettingForProperty(validRateSettings, startDate, endDate);
    });
    log.info("Incremental automatic peak season rate updated");
  }
}
