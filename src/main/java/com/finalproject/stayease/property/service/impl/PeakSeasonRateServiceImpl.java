package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.properties.ConflictingRateException;
import com.finalproject.stayease.exceptions.properties.PeakSeasonRateNotFoundException;
import com.finalproject.stayease.exceptions.properties.PropertyNotFoundException;
import com.finalproject.stayease.exceptions.utils.InvalidDateException;
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
import com.finalproject.stayease.users.entity.Users;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  public PeakSeasonRate setPeakSeasonRate(Long propertyId, SetPeakSeasonRateRequestDTO requestDTO) {
    Property property = propertyService.findPropertyById(propertyId)
        .orElseThrow(() -> new PropertyNotFoundException("Property not found"));
    return setRate(property, requestDTO);
  }

  @Override
  public PeakSeasonRate setPeakSeasonRate(Users tenant, Long propertyId, SetPeakSeasonRateRequestDTO requestDTO) {
    Property property = getProperty(tenant, propertyId);
    checkDateRangeValid(property.getId(), requestDTO.getStartDate(), requestDTO.getEndDate());
    return setRate(property, requestDTO);
  }

  @Override
  public PeakSeasonRate updatePeakSeasonRate(PeakSeasonRate peakSeasonRate, BigDecimal adjustmentRate, AdjustmentType adjustmentType) {
    peakSeasonRate.setAdjustmentRate(adjustmentRate);
    peakSeasonRate.setAdjustmentType(adjustmentType);
    return peakSeasonRateRepository.save(peakSeasonRate);
  }

  @Override
  public PeakSeasonRate updatePeakSeasonRate(Users tenant, Long rateId, SetPeakSeasonRateRequestDTO requestDTO) {
    PeakSeasonRate existingRate = peakSeasonRateRepository.findById(rateId)
        .orElseThrow(() -> new PeakSeasonRateNotFoundException("Peak season rate not found"));
    log.info("Updating peak season rate with ID {}", rateId);

    // Get property while also checking ownership of property and rate
    Property property = getProperty(tenant, existingRate.getProperty().getId());

   return updateRate(existingRate, requestDTO);
  }

  @Override
  public void removePeakSeasonRate(Long rateId) {
    PeakSeasonRate rate = peakSeasonRateRepository.findById(rateId)
        .orElseThrow(() -> new PeakSeasonRateNotFoundException("Peak season rate not found"));
    rate.setDeletedAt(Instant.now());
    peakSeasonRateRepository.save(rate);
    log.info("Deleted peak season rate with ID {}", rateId);
  }

  @Override
  public void removePeakSeasonRate(Users tenant, Long rateId) {
    PeakSeasonRate rate = peakSeasonRateRepository.findById(rateId)
        .orElseThrow(() -> new PeakSeasonRateNotFoundException("Peak season rate not found"));
    Property property = getProperty(tenant, rate.getProperty().getId());
    log.info("Deleting peak season rate with ID {}", rateId);
    rate.setDeletedAt(Instant.now());
    peakSeasonRateRepository.save(rate);
    log.info("Deleted peak season rate with ID {}", rateId);
  }

  @Override
  public int hardDeleteStaleRates(Instant timestamp) {
    return peakSeasonRateRepository.hardDeleteStaleRates(timestamp);
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
  public List<PeakSeasonRate> findValidRatesByPropertyAndDate(Long propertyId, LocalDate startDate, Instant bookingDate,
      Instant endDate) {
    return peakSeasonRateRepository.findValidRatesByPropertyAndDate(propertyId, startDate, bookingDate, endDate);
  }

  @Override
  public List<PeakSeasonRate> findAutomaticRatesByPropertyAndDateRange(Long propertyId, LocalDate startDate,
      LocalDate endDate) {
    return peakSeasonRateRepository.findAutomaticRatesByPropertyAndDateRange(propertyId, startDate, endDate);
  }

  @Override
  public List<RoomAdjustedRatesDTO> findAvailableRoomRates(Long propertyId, LocalDate date) {
    validateDate(date);
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
        // If the room already exists, update the adjusted price if it's higher
        RoomAdjustedRatesDTO existingRoom = adjustedPrices.get(room.getRoomId());
        if (adjustedPrice.compareTo(existingRoom.getAdjustedPrice()) > 0) {
          existingRoom.setAdjustedPrice(adjustedPrice);
        }
      }
    }

    log.info("Found {} available room rates for property {} on date {}", adjustedPrices.size(), propertyId, date);
    return new ArrayList<>(adjustedPrices.values());
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

    BigDecimal totalAdjustment = BigDecimal.ZERO;

    for (PeakSeasonRate rate : applicableRates) {
      if (rate.getAdjustmentType() == AdjustmentType.PERCENTAGE) {
        BigDecimal percentageAdjustment = basePrice.multiply(rate.getAdjustmentRate().divide(BigDecimal.valueOf(100)));
        totalAdjustment = totalAdjustment.add(percentageAdjustment);
      } else {
        totalAdjustment = totalAdjustment.add(Optional.ofNullable(rate.getAdjustmentRate()).orElse(BigDecimal.ZERO));
      }
    }

    BigDecimal adjustedPrice = basePrice.add(totalAdjustment);
    return adjustedPrice.setScale(2, RoundingMode.HALF_UP);
  }

  private Property getProperty(Users tenant, Long propertyId) {
    Property property = propertyService.findPropertyById(propertyId)
        .orElseThrow(() -> new PropertyNotFoundException("Property not found"));
    if (property.getTenant() != tenant) {
      throw new BadCredentialsException("You are not the owner of this property");
    }
    return property;
  }

  private PeakSeasonRate setRate(Property property, SetPeakSeasonRateRequestDTO requestDTO) {
    PeakSeasonRate peakSeasonRate = new PeakSeasonRate();
    peakSeasonRate.setProperty(property);
    peakSeasonRate.setStartDate(requestDTO.getStartDate());
    peakSeasonRate.setEndDate(requestDTO.getEndDate());
    peakSeasonRate.setAdjustmentRate(requestDTO.getAdjustmentRate());
    peakSeasonRate.setAdjustmentType(requestDTO.getAdjustmentType());
    peakSeasonRate.setReason(requestDTO.getReason());
    peakSeasonRateRepository.save(peakSeasonRate);
    return peakSeasonRate;
  }

  private void checkDateRangeValid(Long propertyId, LocalDate startDate, LocalDate endDate) {
    boolean existsConflictingRate = peakSeasonRateRepository.existsConflictingRate(propertyId, startDate, endDate);
    if (existsConflictingRate) {
      throw new ConflictingRateException("Rates for this date range already set");
    }
  }

  private LocalDate isUpdateDateValid(SetPeakSeasonRateRequestDTO requestDTO) {
    LocalDate startDate = requestDTO.getStartDate();
    if (startDate != null) {
      if (LocalDate.now().isAfter(startDate)) {
        throw new InvalidDateException("Start date cannot be changed");
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

    existingRate.setReason(Optional.ofNullable(requestDTO.getReason())
        .orElse(existingRate.getReason()));

    log.info("Updated peak season rate with ID {}", existingRate.getId());
    return peakSeasonRateRepository.save(existingRate);
  }

  private void validateDate(LocalDate date) {
    if (date.isBefore(LocalDate.now())) {
      throw new InvalidDateException("Date is out of valid range: " + date);
    }
  }

  private void validateDate(LocalDate startDate, LocalDate endDate) {
    validateDate(startDate);
    if (startDate.isAfter(endDate)) {
      throw new InvalidDateException("Start date cannot be after end date");
    }
  }
}
