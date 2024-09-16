package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPeakSeasonRateRequestDTO;
import com.finalproject.stayease.property.repository.PeakSeasonRateRepository;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.users.entity.Users;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
@Slf4j
public class PeakSeasonRateServiceImpl implements PeakSeasonRateService {

  private final PeakSeasonRateRepository peakSeasonRateRepository;
  private final PropertyService propertyService;

  @Override
  public PeakSeasonRate setPeakSeasonRate(Users tenant, Long propertyId, SetPeakSeasonRateRequestDTO requestDTO) {
    Property property = getProperty(tenant, propertyId);
    return setRate(property, requestDTO);
  }

  @Override
  public PeakSeasonRate updatePeakSeasonRate(Users tenant, Long propertyId, Long rateId, SetPeakSeasonRateRequestDTO requestDTO) {
    Property property = getProperty(tenant, propertyId);

    // TODO PeakSeasonRateNotFoundException
    PeakSeasonRate existingRate = peakSeasonRateRepository.findById(rateId).orElseThrow(() -> new DataNotFoundException("Peak Season Rate Not Found"));
    existingRate.setValidTo(Instant.now());
    peakSeasonRateRepository.save(existingRate);

    return updateRate(property, existingRate, requestDTO);
  }

  @Override
  public BigDecimal applyPeakSeasonRate(Long propertyId, LocalDate date, BigDecimal basePrice, Instant bookingTime) {
    List<PeakSeasonRate> applicableRates = peakSeasonRateRepository
        .findValidRatesByPropertyAndDate(propertyId, date, bookingTime, Instant.MAX);

    BigDecimal adjustedPrice = basePrice;
    for (PeakSeasonRate rate : applicableRates) {
      if (rate.getAdjustmentType() == AdjustmentType.PERCENTAGE) {
        adjustedPrice =
            adjustedPrice.add(basePrice.multiply(rate.getRateAdjustment().divide(BigDecimal.valueOf(100))));
      } else {
        adjustedPrice = adjustedPrice.add(rate.getRateAdjustment());
      }
    }
    return adjustedPrice.setScale(2, RoundingMode.HALF_UP);
  }

  private Property getProperty(Users tenant, Long propertyId) {
    // TODO : PropertyNotFoundException
    Property property = propertyService.findPropertyById(propertyId)
        .orElseThrow(() -> new DataNotFoundException("Property not found"));
    if (property.getTenant() != tenant) {
      throw new BadCredentialsException("You are not the owner of this property");
    }
    return property;
  }

  private PeakSeasonRate setRate(Property property, SetPeakSeasonRateRequestDTO requestDTO) {
    isDateValid(requestDTO.getStartDate(), requestDTO.getEndDate());
    PeakSeasonRate peakSeasonRate = new PeakSeasonRate();
    peakSeasonRate.setProperty(property);
    peakSeasonRate.setStartDate(requestDTO.getStartDate());
    peakSeasonRate.setEndDate(requestDTO.getEndDate());
    peakSeasonRate.setRateAdjustment(requestDTO.getRateAdjustment());
    peakSeasonRate.setAdjustmentType(requestDTO.getAdjustmentType());
    peakSeasonRateRepository.save(peakSeasonRate);
    return peakSeasonRate;
  }

  private void isDateValid(LocalDate startDate, LocalDate endDate) {
    Optional<PeakSeasonRate> rangeOptional = peakSeasonRateRepository.findByStartDateAndEndDate(startDate, endDate);
    if (rangeOptional.isPresent()) {
      throw new DuplicateEntryException("Rate for this range of date for your property is already set");
    }
  }

  private LocalDate isUpdateDateValid(SetPeakSeasonRateRequestDTO requestDTO) {
    LocalDate startDate = requestDTO.getStartDate();
    if (startDate != null) {
      if (startDate.isAfter(LocalDate.now())) {
        // TODO InvalidDateException
        throw new BadCredentialsException("Start date cannot be changed");
      } else {
        return startDate;
      }
    }
    return null;
  }

  private PeakSeasonRate updateRate(Property property, PeakSeasonRate existingRate, SetPeakSeasonRateRequestDTO requestDTO) {
    PeakSeasonRate updatedRate = new PeakSeasonRate();
    updatedRate.setProperty(property);

    updatedRate.setStartDate(Optional.ofNullable(isUpdateDateValid(requestDTO))
        .orElse(existingRate.getStartDate()));

    updatedRate.setEndDate(Optional.ofNullable(requestDTO.getEndDate())
        .orElse(existingRate.getEndDate()));

    updatedRate.setRateAdjustment(Optional.ofNullable(requestDTO.getRateAdjustment())
        .orElse(existingRate.getRateAdjustment()));

    updatedRate.setAdjustmentType(Optional.ofNullable(requestDTO.getAdjustmentType())
        .orElse(existingRate.getAdjustmentType()));

    return peakSeasonRateRepository.save(updatedRate);
  }
}
