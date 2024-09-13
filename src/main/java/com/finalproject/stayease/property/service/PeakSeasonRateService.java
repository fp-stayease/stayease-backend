package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.dto.DailyPriceDTO;
import com.finalproject.stayease.property.entity.dto.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPeakSeasonRateRequestDTO;
import com.finalproject.stayease.users.entity.Users;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface PeakSeasonRateService {
  PeakSeasonRate setPeakSeasonRate(Users tenant, Long propertyId, SetPeakSeasonRateRequestDTO requestDTO);
  PeakSeasonRate updatePeakSeasonRate(Users tenant, Long propertyId, Long rateId, SetPeakSeasonRateRequestDTO requestDTO);

  List<RoomAdjustedRatesDTO> findAvailableRoomRates(Long propertyId, LocalDate date);

  List<DailyPriceDTO> findLowestDailyRoomRates(Long propertyId, LocalDate startDate, LocalDate endDate);

  List<DailyPriceDTO> findCumulativeRoomRates(Long propertyId, LocalDate startDate, LocalDate endDate);

  // Region - price adjustments
  BigDecimal applyPeakSeasonRate(Long propertyId, LocalDate date, BigDecimal basePrice, Instant bookingTime);

}
