package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.listingDTOs.DailyPriceDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
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
import java.time.ZoneOffset;
import java.util.ArrayList;
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
  public PeakSeasonRate updatePeakSeasonRate(Users tenant, Long rateId, SetPeakSeasonRateRequestDTO requestDTO) {
    // TODO PeakSeasonRateNotFoundException
    PeakSeasonRate existingRate = peakSeasonRateRepository.findById(rateId)
        .orElseThrow(() -> new DataNotFoundException("Peak season rate not found"));
    log.info("Updating peak season rate with ID {}", rateId);

    // Get property while also checking ownership of property and rate
    Property property = getProperty(tenant, existingRate.getProperty().getId());

   return updateRate(existingRate, requestDTO);
  }

  @Override
  public List<PeakSeasonRate> getTenantCurrentRates(Users tenant) {
    List<Property> properties = propertyService.findAllByTenant(tenant);
    List<PeakSeasonRate> currentRates = new ArrayList<>();
    for (Property property : properties) {
      List<PeakSeasonRate> rates = peakSeasonRateRepository.findByPropertyAndEndDateAfterAndDeletedAtIsNull(property,
          LocalDate.now());
      currentRates.addAll(rates);
    }
    return currentRates;
  }

  @Override
  public List<RoomAdjustedRatesDTO> findAvailableRoomRates(Long propertyId, LocalDate date) {
    validateDate(date);
    log.info("Finding available room rates for property {} on date {}", propertyId, date);
    List<RoomPriceRateDTO> rooms = propertyService.findAvailableRoomRates(propertyId, date);
    List<RoomAdjustedRatesDTO> adjustedPrices = new ArrayList<>();
    for (RoomPriceRateDTO room : rooms) {
      BigDecimal adjustedPrice = applyPeakSeasonRate(room);
      adjustedPrices.add(new RoomAdjustedRatesDTO(room.getPropertyId(), room.getRoomId(), room.getRoomName(),
          room.getImageUrl(), room.getRoomCapacity(), room.getRoomDescription(),
          room.getBasePrice(), adjustedPrice, date, room.getIsAvailable()));
    }
    log.info("Found {} available room rates for property {} on date {}", adjustedPrices.size(), propertyId, date);
    return adjustedPrices;
  }

  @Override
  public List<DailyPriceDTO> findLowestDailyRoomRates(Long propertyId, LocalDate startDate, LocalDate endDate) {
    validateDate(startDate, endDate);
    List<DailyPriceDTO> dailyPrices = new ArrayList<>();
    for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
      RoomPriceRateDTO lowestRoomRate = propertyService.findLowestRoomRate(propertyId, date);
      BigDecimal lowestPrice = applyPeakSeasonRate(lowestRoomRate);
      dailyPrices.add(new DailyPriceDTO(date, lowestPrice, !lowestRoomRate.getBasePrice().equals(lowestPrice)));
    }
    return dailyPrices;
  }

  @Override
  public List<DailyPriceDTO> findCumulativeRoomRates(Long propertyId, LocalDate startDate, LocalDate endDate) {
    validateDate(startDate, endDate);
    List<DailyPriceDTO> cumulativeDailyPrice = new ArrayList<>();
    for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
      List<DailyPriceDTO> lowestDailyRoomRates = findLowestDailyRoomRates(propertyId, startDate, date.plusDays(1));
      BigDecimal cumulativePrice = lowestDailyRoomRates.stream()
          .map(DailyPriceDTO::getLowestPrice)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      cumulativeDailyPrice.add(new DailyPriceDTO(date, cumulativePrice, lowestDailyRoomRates.getLast()
          .isHasAdjustment()));
    }
    return cumulativeDailyPrice;
  }



  @Override
  public BigDecimal applyPeakSeasonRate(RoomPriceRateDTO roomRate) {
    BigDecimal adjustedPrice = roomRate.getBasePrice();
    adjustedPrice = roomRate.getAdjustmentType() == AdjustmentType.PERCENTAGE
        ? adjustedPrice.add(adjustedPrice.multiply(roomRate.getAdjustmentRate().divide(BigDecimal.valueOf(100))))
        : adjustedPrice.add(Optional.ofNullable(roomRate.getAdjustmentRate()).orElse(BigDecimal.ZERO));
    return adjustedPrice.setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal applyPeakSeasonRate(Long propertyId, LocalDate date, BigDecimal basePrice, Instant bookingTime) {
    validateDate(date);
    Instant futureDate = LocalDate.now().plusYears(10).atStartOfDay().toInstant(ZoneOffset.UTC);

    List<PeakSeasonRate> applicableRates = peakSeasonRateRepository
        .findValidRatesByPropertyAndDate(propertyId, date, bookingTime, futureDate);

    BigDecimal adjustedPrice = basePrice;
    for (PeakSeasonRate rate : applicableRates) {
      adjustedPrice = rate.getAdjustmentType() == AdjustmentType.PERCENTAGE
          ? adjustedPrice.add(basePrice.multiply(rate.getAdjustmentRate().divide(BigDecimal.valueOf(100))))
          : adjustedPrice.add(Optional.ofNullable(rate.getAdjustmentRate()).orElse(BigDecimal.ZERO));
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
    checkDateRangeValid(property.getId(), requestDTO.getStartDate(), requestDTO.getEndDate());
    PeakSeasonRate peakSeasonRate = new PeakSeasonRate();
    peakSeasonRate.setProperty(property);
    peakSeasonRate.setStartDate(requestDTO.getStartDate());
    peakSeasonRate.setEndDate(requestDTO.getEndDate());
    peakSeasonRate.setAdjustmentRate(requestDTO.getAdjustmentRate());
    peakSeasonRate.setAdjustmentType(requestDTO.getAdjustmentType());
    peakSeasonRateRepository.save(peakSeasonRate);
    return peakSeasonRate;
  }

  private void checkDateRangeValid(Long propertyId, LocalDate startDate, LocalDate endDate) {
    boolean existsConflictingRate = peakSeasonRateRepository.existsConflictingRate(propertyId, startDate, endDate);
    if (existsConflictingRate) {
      throw new DuplicateEntryException("Rates for this date range already set");
    }
  }

  private LocalDate isUpdateDateValid(SetPeakSeasonRateRequestDTO requestDTO) {
    LocalDate startDate = requestDTO.getStartDate();
    if (startDate != null) {
      if (LocalDate.now().isAfter(startDate)) {
        // TODO InvalidDateException
        throw new BadCredentialsException("Start date cannot be changed");
      } else {
        return startDate;
      }
    }
    return null;
  }

  private PeakSeasonRate updateRate(PeakSeasonRate existingRate, SetPeakSeasonRateRequestDTO requestDTO) {
    existingRate.setStartDate(Optional.ofNullable(isUpdateDateValid(requestDTO))
        .orElse(existingRate.getStartDate()));

    existingRate.setEndDate(Optional.ofNullable(requestDTO.getEndDate())
        .orElse(existingRate.getEndDate()));

    existingRate.setAdjustmentRate(Optional.ofNullable(requestDTO.getAdjustmentRate())
        .orElse(existingRate.getAdjustmentRate()));

    existingRate.setAdjustmentType(Optional.ofNullable(requestDTO.getAdjustmentType())
        .orElse(existingRate.getAdjustmentType()));

    return peakSeasonRateRepository.save(existingRate);
  }

  private void validateDate(LocalDate date) {
    if (date.isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("Date is out of valid range: " + date);
    }
  }

  private void validateDate(LocalDate startDate, LocalDate endDate) {
    validateDate(startDate);
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date cannot be after end date");
    }
  }
}
