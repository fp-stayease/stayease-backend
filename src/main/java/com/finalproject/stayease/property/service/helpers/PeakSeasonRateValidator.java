package com.finalproject.stayease.property.service.helpers;

import com.finalproject.stayease.exceptions.auth.UnauthorizedOperationsException;
import com.finalproject.stayease.exceptions.properties.ConflictingRateException;
import com.finalproject.stayease.exceptions.properties.PeakSeasonRateNotFoundException;
import com.finalproject.stayease.exceptions.properties.PropertyNotFoundException;
import com.finalproject.stayease.exceptions.utils.InvalidDateException;
import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.repository.PeakSeasonRateRepository;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.users.entity.Users;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PeakSeasonRateValidator {
  private final PeakSeasonRateRepository peakSeasonRateRepository;
  private final PropertyService propertyService;

  // Validates that the given date is not in the past
  public void validateDate(LocalDate date) {
    if (date.isBefore(LocalDate.now())) {
      throw new InvalidDateException("Date is out of valid range: " + date);
    }
  }

  // Validates that the start date is not after the end date
  public void validateDateRange(LocalDate startDate, LocalDate endDate) {
    validateDate(startDate);
    if (startDate.isAfter(endDate)) {
      throw new InvalidDateException("Start date cannot be after end date");
    }
  }

  // Validates that there are no conflicting rates for the given date range
  public void validateRateDateRange(Long propertyId, LocalDate startDate, LocalDate endDate) {
    validateDateRange(startDate, endDate);
    boolean existsConflictingRate = peakSeasonRateRepository.existsConflictingRate(propertyId, startDate, endDate);
    if (existsConflictingRate) {
      throw new ConflictingRateException("Rates for this date range already set");
    }
  }

  // Validates that the tenant owns the property
  public Property validatePropertyOwnership(Users tenant, Long propertyId) {
    Property property = propertyService.findPropertyById(propertyId)
        .orElseThrow(() -> new PropertyNotFoundException("Property not found"));
    if (!property.getTenant().equals(tenant)) {
      throw new UnauthorizedOperationsException("You are not the owner of this property");
    }
    return property;
  }

  // Retrieve property and validate if it exists
  public Property checkAndRetrieveProperty(Long propertyId) {
    return propertyService.findPropertyById(propertyId)
        .orElseThrow(() -> new PropertyNotFoundException("Property not found"));
  }

  // Retrieve peak season rate and validate if it exists
  public PeakSeasonRate checkAndRetrievePeakSeasonRate(Long rateId) {
    return peakSeasonRateRepository.findById(rateId)
        .orElseThrow(() -> new PeakSeasonRateNotFoundException("Peak season rate not found"));
  }

}
