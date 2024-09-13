package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.DailyPriceDTO;
import com.finalproject.stayease.property.entity.dto.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.entity.dto.RoomPriceRateDTO;
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
import java.util.Comparator;
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
  public List<RoomAdjustedRatesDTO> findAvailableRoomRates(Long propertyId, LocalDate date) {
    log.info("Finding available room rates for property {} on date {}", propertyId, date);
    List<RoomPriceRateDTO> rooms = propertyService.findAvailableRoomRates(propertyId, date);
    List<RoomAdjustedRatesDTO> adjustedPrices = new ArrayList<>();
    for (RoomPriceRateDTO room : rooms) {
      BigDecimal adjustedPrice = applyPeakSeasonRate(room);
      adjustedPrices.add(new RoomAdjustedRatesDTO(room.getPropertyId(), room.getRoomId(), room.getRoomName(),
          room.getBasePrice(), adjustedPrice, date));
    }
    log.info("Found {} available room rates for property {} on date {}", adjustedPrices.size(), propertyId, date);
    return adjustedPrices;
  }

  @Override
  public List<DailyPriceDTO> findAvailableDailyRoomRates(Long propertyId, LocalDate startDate, LocalDate endDate) {
    List<DailyPriceDTO> dailyPrices = new ArrayList<>();
    for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
      List<RoomAdjustedRatesDTO> roomRates = findAvailableRoomRates(propertyId, date);
      BigDecimal lowestPrice = roomRates.stream()
          .map(RoomAdjustedRatesDTO::getAdjustedPrice)
          .min(Comparator.naturalOrder())
          .orElse(BigDecimal.ZERO);
      dailyPrices.add(new DailyPriceDTO(date, lowestPrice, !roomRates.isEmpty() && !roomRates.getFirst().getBasePrice().equals(lowestPrice)));
    }
    return dailyPrices;
  }

  @Override
  public List<DailyPriceDTO> findLowestDailyRoomRates(Long propertyId, LocalDate startDate, LocalDate endDate) {
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

  private BigDecimal applyPeakSeasonRate(RoomPriceRateDTO roomRate) {
    BigDecimal adjustedPrice = roomRate.getBasePrice();
    adjustedPrice = roomRate.getAdjustmentType() == AdjustmentType.PERCENTAGE
        ? adjustedPrice.add(adjustedPrice.multiply(roomRate.getAdjustmentRate().divide(BigDecimal.valueOf(100))))
        : adjustedPrice.add(Optional.ofNullable(roomRate.getAdjustmentRate()).orElse(BigDecimal.ZERO));
    return adjustedPrice.setScale(2, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal applyPeakSeasonRate(Long propertyId, LocalDate date, BigDecimal basePrice, Instant bookingTime) {
    Instant futureDate = LocalDate.now().plusYears(10).atStartOfDay().toInstant(ZoneOffset.UTC);

    List<PeakSeasonRate> applicableRates = peakSeasonRateRepository
        .findValidRatesByPropertyAndDate(propertyId, date, bookingTime, futureDate);

    BigDecimal adjustedPrice = basePrice;
    for (PeakSeasonRate rate : applicableRates) {
      adjustedPrice = rate.getAdjustmentType() == AdjustmentType.PERCENTAGE
          ? adjustedPrice.add(basePrice.multiply(rate.getRateAdjustment().divide(BigDecimal.valueOf(100))))
          : adjustedPrice.add(Optional.ofNullable(rate.getRateAdjustment()).orElse(BigDecimal.ZERO));
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
