package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPeakSeasonRateRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.DailyPriceDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import com.finalproject.stayease.users.entity.Users;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface PeakSeasonRateService {

  // Region - basic CRUD operations

  PeakSeasonRate setPeakSeasonRate(Long propertyId, SetPeakSeasonRateRequestDTO requestDTO);

  PeakSeasonRate setPeakSeasonRate(Users tenant, Long propertyId, SetPeakSeasonRateRequestDTO requestDTO);

  PeakSeasonRate updatePeakSeasonRate(PeakSeasonRate peakSeasonRate, BigDecimal adjustmentRate,
      AdjustmentType adjustmentType);

  PeakSeasonRate updatePeakSeasonRate(Users tenant, Long rateId, SetPeakSeasonRateRequestDTO requestDTO);

  void deletePeakSeasonRate(Long rateId);

  void deletePeakSeasonRate(Users tenant, Long rateId);

  int hardDeleteStaleRates(Instant timestamp);

  // Region - query operations

  List<PeakSeasonRate> getTenantCurrentRates(Users tenant);

  List<PeakSeasonRate> findValidRatesByPropertyAndDate(Long propertyId, LocalDate startDate,
      Instant bookingDate, Instant endDate);

  List<PeakSeasonRate> findAutomaticRatesByPropertyAndDateRange(Long propertyId, LocalDate startDate, LocalDate endDate);

  List<RoomAdjustedRatesDTO> findAvailableRoomRates(Long propertyId, LocalDate date);

  List<DailyPriceDTO> findLowestDailyRoomRates(Long propertyId, LocalDate startDate, LocalDate endDate);

  List<DailyPriceDTO> findCumulativeRoomRates(Long propertyId, LocalDate startDate, LocalDate endDate);


  BigDecimal applyPeakSeasonRate(RoomPriceRateDTO roomRate);

  // Region - price adjustments
  BigDecimal applyPeakSeasonRate(Long propertyId, LocalDate date, BigDecimal basePrice, Instant bookingTime);

}
