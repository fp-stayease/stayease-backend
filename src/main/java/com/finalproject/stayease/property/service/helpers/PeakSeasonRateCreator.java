package com.finalproject.stayease.property.service.helpers;

import com.finalproject.stayease.exceptions.utils.InvalidDateException;
import com.finalproject.stayease.exceptions.utils.InvalidRequestException;
import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPeakSeasonRateRequestDTO;
import com.finalproject.stayease.property.repository.PeakSeasonRateRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PeakSeasonRateCreator {
  private final PeakSeasonRateRepository peakSeasonRateRepository;

  /**
   * Creates a new PeakSeasonRate entity from the provided data.
   * @param property The Property to associate with the rate
   * @param requestDTO The DTO containing rate details
   * @return The created PeakSeasonRate
   */
  public PeakSeasonRate createRate(Property property, SetPeakSeasonRateRequestDTO requestDTO) {
    // Check if the adjustment type is percentage and if the rate exceeds 100
    if (AdjustmentType.PERCENTAGE.equals(requestDTO.getAdjustmentType())
        && requestDTO.getAdjustmentRate().compareTo(BigDecimal.valueOf(100)) > 0) {
      throw new InvalidRequestException("Adjustment rate cannot exceed 100% for percentage-based adjustments.");
    }

    // Create and populate PeakSeasonRate object
    PeakSeasonRate peakSeasonRate = new PeakSeasonRate();
    peakSeasonRate.setProperty(property);
    peakSeasonRate.setStartDate(requestDTO.getStartDate());
    peakSeasonRate.setEndDate(requestDTO.getEndDate());
    peakSeasonRate.setAdjustmentRate(requestDTO.getAdjustmentRate());
    peakSeasonRate.setAdjustmentType(requestDTO.getAdjustmentType());
    peakSeasonRate.setReason(requestDTO.getReason());
    return peakSeasonRateRepository.save(peakSeasonRate);
  }

  public PeakSeasonRate updateRate(PeakSeasonRate existingRate, SetPeakSeasonRateRequestDTO requestDTO) {
    existingRate.setStartDate(Optional.ofNullable(validateUpdateDate(existingRate.getStartDate(), requestDTO.getStartDate()))
        .orElse(existingRate.getStartDate()));

    existingRate.setEndDate(Optional.ofNullable(requestDTO.getEndDate())
        .orElse(existingRate.getEndDate()));

    existingRate.setAdjustmentRate(Optional.ofNullable(requestDTO.getAdjustmentRate())
        .orElse(existingRate.getAdjustmentRate()));

    existingRate.setAdjustmentType(Optional.ofNullable(requestDTO.getAdjustmentType())
        .orElse(existingRate.getAdjustmentType()));

    existingRate.setReason(Optional.ofNullable(requestDTO.getReason())
        .orElse(existingRate.getReason()));

    log.info("Updated peak season rate with ID {}", existingRate.getId());
    return peakSeasonRateRepository.save(existingRate);
  }

  private LocalDate validateUpdateDate(LocalDate existingStartDate, LocalDate requestedStartDate) {
    if (requestedStartDate == null) {
      return null;
    }

    if (existingStartDate.equals(requestedStartDate)) {
      return requestedStartDate; // No change needed
    }

    if (LocalDate.now().isAfter(existingStartDate)) {
      throw new InvalidDateException("Start date cannot be changed");
    }

    return requestedStartDate;
  }

}