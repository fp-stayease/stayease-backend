package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPeakSeasonRateRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.DailyPriceDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import com.finalproject.stayease.property.repository.PeakSeasonRateRepository;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.helpers.PeakSeasonRateCalculator;
import com.finalproject.stayease.property.service.helpers.PeakSeasonRateCreator;
import com.finalproject.stayease.property.service.helpers.PeakSeasonRateValidator;
import com.finalproject.stayease.users.entity.Users;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
@Slf4j
public class PeakSeasonRateServiceImpl implements PeakSeasonRateService {

  private final PeakSeasonRateRepository peakSeasonRateRepository;
  private final PropertyService propertyService;
  private final PeakSeasonRateValidator validator;
  private final PeakSeasonRateCreator creator;
  private final PeakSeasonRateCalculator calculator;

  // Basic CRUD operations

  /**
   * Sets a new peak season rate for a property.
   * @param propertyId The ID of the property
   * @param requestDTO The DTO containing rate details
   * @return The created PeakSeasonRate
   */
  @Override
  public PeakSeasonRate setPeakSeasonRate(Long propertyId, SetPeakSeasonRateRequestDTO requestDTO) {
    Property property = validator.checkAndRetrieveProperty(propertyId);
    return creator.createRate(property, requestDTO);
  }

  /**
   * Sets a new peak season rate for a property owned by a specific tenant.
   * @param tenant The tenant user
   * @param propertyId The ID of the property
   * @param requestDTO The DTO containing rate details
   * @return The created PeakSeasonRate
   */
  @Override
  public PeakSeasonRate setPeakSeasonRate(Users tenant, Long propertyId, SetPeakSeasonRateRequestDTO requestDTO) {
    Property property = validator.validatePropertyOwnership(tenant, propertyId);
    validator.validateRateDateRange(property.getId(), requestDTO.getStartDate(), requestDTO.getEndDate());
    return creator.createRate(property, requestDTO);
  }

  /**
   * Updates an existing peak season rate.
   * @param peakSeasonRate The PeakSeasonRate to update
   * @param adjustmentRate The new adjustment rate
   * @param adjustmentType The new adjustment type
   * @return The updated PeakSeasonRate
   */
  @Override
  public PeakSeasonRate updatePeakSeasonRate(PeakSeasonRate peakSeasonRate, BigDecimal adjustmentRate, AdjustmentType adjustmentType) {
    peakSeasonRate.setAdjustmentRate(adjustmentRate);
    peakSeasonRate.setAdjustmentType(adjustmentType);
    return peakSeasonRateRepository.save(peakSeasonRate);
  }

  /**
   * Updates an existing peak season rate for a property owned by a specific tenant.
   * @param tenant The tenant user
   * @param rateId The ID of the rate to update
   * @param requestDTO The DTO containing updated rate details
   * @return The updated PeakSeasonRate
   */
  @Override
  public PeakSeasonRate updatePeakSeasonRate(Users tenant, Long rateId, SetPeakSeasonRateRequestDTO requestDTO) {
    PeakSeasonRate existingRate = validator.checkAndRetrievePeakSeasonRate(rateId);
    log.info("Updating peak season rate with ID {}", rateId);
    validator.validatePropertyOwnership(tenant, existingRate.getProperty().getId());
    return creator.updateRate(existingRate, requestDTO);
  }

  /**
   * Removes (soft deletes) a peak season rate.
   * @param rateId The ID of the rate to remove
   */
  @Override
  public void removePeakSeasonRate(Long rateId) {
    PeakSeasonRate rate = validator.checkAndRetrievePeakSeasonRate(rateId);
    rate.setDeletedAt(Instant.now());
    peakSeasonRateRepository.save(rate);
  }

  /**
   * Removes (soft deletes) a peak season rate for a property owned by a specific tenant.
   * @param tenant The tenant user
   * @param rateId The ID of the rate to remove
   */
  @Override
  public void removePeakSeasonRate(Users tenant, Long rateId) {
    PeakSeasonRate rate = validator.checkAndRetrievePeakSeasonRate(rateId);
    validator.validatePropertyOwnership(tenant, rate.getProperty().getId());
    log.info("Deleting peak season rate with ID {}", rateId);
    rate.setDeletedAt(Instant.now());
    peakSeasonRateRepository.save(rate);
    log.info("Deleted peak season rate with ID {}", rateId);
  }

  /**
   * Permanently deletes stale peak season rates.
   * @param timestamp The timestamp before which rates are considered stale
   * @return The number of deleted rates
   */
  @Override
  public int hardDeleteStaleRates(Instant timestamp) {
    return peakSeasonRateRepository.hardDeleteStaleRates(timestamp);
  }

  // Query operations

  /**
   * Retrieves current rates for all properties owned by a tenant.
   * @param tenant The tenant user
   * @return List of current PeakSeasonRates
   */
  @Override
  public List<PeakSeasonRate> getTenantCurrentRates(Users tenant) {
    List<Property> properties = propertyService.findAllByTenant(tenant);
    List<PeakSeasonRate> currentRates = new ArrayList<>();
    for (Property property : properties) {
      List<PeakSeasonRate> rates = peakSeasonRateRepository.findByPropertyAndEndDateAfterAndDeletedAtIsNull(property, LocalDate.now());
      currentRates.addAll(rates);
    }
    return currentRates;
  }

  /**
   * Finds valid rates for a property on a specific date.
   * @param propertyId The ID of the property
   * @param startDate The start date
   * @param bookingDate The booking date
   * @param endDate The end date
   * @return List of valid PeakSeasonRates
   */
  @Override
  public List<PeakSeasonRate> findValidRatesByPropertyAndDate(Long propertyId, LocalDate startDate, Instant bookingDate, Instant endDate) {
    return peakSeasonRateRepository.findValidRatesByPropertyAndDate(propertyId, startDate, bookingDate, endDate);
  }

  /**
   * Finds automatic rates for a property within a date range.
   * @param propertyId The ID of the property
   * @param startDate The start date of the range
   * @param endDate The end date of the range
   * @return List of automatic PeakSeasonRates
   */
  @Override
  public List<PeakSeasonRate> findAutomaticRatesByPropertyAndDateRange(Long propertyId, LocalDate startDate, LocalDate endDate) {
    return peakSeasonRateRepository.findAutomaticRatesByPropertyAndDateRange(propertyId, startDate, endDate);
  }

  /**
   * Finds available room rates with adjustments for a property on a specific date.
   * @param propertyId The ID of the property
   * @param date The date to check
   * @return List of RoomAdjustedRatesDTO
   */
  @Override
  public List<RoomAdjustedRatesDTO> findAvailableRoomRates(Long propertyId, LocalDate date) {
    validator.validateDate(date);
    log.info("Finding available room rates for property {} on date {}", propertyId, date);
    List<RoomPriceRateDTO> rooms = propertyService.findAvailableRoomRates(propertyId, date);

    Map<Long, RoomAdjustedRatesDTO> adjustedPrices = new HashMap<>();

    for (RoomPriceRateDTO room : rooms) {
      BigDecimal adjustedPrice = applyPeakSeasonRate(room.getPropertyId(), date, room.getBasePrice(), Instant.now());

      if (!adjustedPrices.containsKey(room.getRoomId())) {
        adjustedPrices.put(room.getRoomId(), new RoomAdjustedRatesDTO(
            room.getPropertyId(), room.getRoomId(), room.getRoomName(),
            room.getImageUrl(), room.getRoomCapacity(), room.getRoomDescription(),
            room.getBasePrice(), adjustedPrice, date, room.getIsAvailable()
        ));
      } else {
        RoomAdjustedRatesDTO existingRoom = adjustedPrices.get(room.getRoomId());
        if (adjustedPrice.compareTo(existingRoom.getAdjustedPrice()) > 0) {
          existingRoom.setAdjustedPrice(adjustedPrice);
        }
      }
    }

    log.info("Found {} available room rates for property {} on date {}", adjustedPrices.size(), propertyId, date);
    return new ArrayList<>(adjustedPrices.values());
  }

  /**
   * Finds the lowest daily room rates for a property within a date range.
   * @param propertyId The ID of the property
   * @param startDate The start date of the range
   * @param endDate The end date of the range
   * @return List of DailyPriceDTO
   */
  @Override
  public List<DailyPriceDTO> findLowestDailyRoomRates(Long propertyId, LocalDate startDate, LocalDate endDate) {
    validator.validateDateRange(startDate, endDate);
    List<DailyPriceDTO> dailyPrices = new ArrayList<>();
    for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
      RoomPriceRateDTO lowestRoomRate = propertyService.findLowestRoomRate(propertyId, date);
      BigDecimal lowestPrice = applyPeakSeasonRate(lowestRoomRate);
      dailyPrices.add(new DailyPriceDTO(date, lowestPrice, lowestRoomRate.getBasePrice().compareTo(lowestPrice) != 0));
    }
    return dailyPrices;
  }

  /**
   * Finds the cumulative room rates for a property within a date range.
   * @param propertyId The ID of the property
   * @param startDate The start date of the range
   * @param endDate The end date of the range
   * @return List of DailyPriceDTO with cumulative prices
   */
  @Override
  public List<DailyPriceDTO> findCumulativeRoomRates(Long propertyId, LocalDate startDate, LocalDate endDate) {
    validator.validateDateRange(startDate, endDate);
    List<DailyPriceDTO> cumulativeDailyPrice = new ArrayList<>();
    for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
      List<DailyPriceDTO> lowestDailyRoomRates = findLowestDailyRoomRates(propertyId, startDate, date.plusDays(1));
      BigDecimal cumulativePrice = lowestDailyRoomRates.stream()
          .map(DailyPriceDTO::getLowestPrice)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      cumulativeDailyPrice.add(new DailyPriceDTO(date, cumulativePrice, lowestDailyRoomRates.getLast().isHasAdjustment()));
    }
    return cumulativeDailyPrice;
  }

  // Price adjustments

  /**
   * Applies peak season rate adjustments to a room rate.
   * @param roomRate The RoomPriceRateDTO to adjust
   * @return The adjusted price
   */
  @Override
  public BigDecimal applyPeakSeasonRate(RoomPriceRateDTO roomRate) {
    return calculator.applyPeakSeasonRate(roomRate);
  }

  /**
   * Applies peak season rate adjustments to a base price for a property on a specific date.
   * @param propertyId The ID of the property
   * @param date The date to check
   * @param basePrice The base price to adjust
   * @param bookingTime The time of booking
   * @return The adjusted price
   */
  @Override
  public BigDecimal applyPeakSeasonRate(Long propertyId, LocalDate date, BigDecimal basePrice, Instant bookingTime) {
    validator.validateDate(date);
    Instant futureDate = LocalDate.now().plusYears(10).atStartOfDay().toInstant(ZoneOffset.UTC);

    List<PeakSeasonRate> applicableRates = peakSeasonRateRepository
        .findValidRatesByPropertyAndDate(propertyId, date, bookingTime, futureDate);

    return calculator.calculateAdjustedPrice(basePrice, applicableRates);
  }
}
