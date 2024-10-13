package com.finalproject.stayease.property.service.helpers;

import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PeakSeasonRateCalculator {

  public BigDecimal applyPeakSeasonRate(RoomPriceRateDTO roomRate) {
    BigDecimal adjustedPrice = roomRate.getBasePrice();
    adjustedPrice = roomRate.getAdjustmentType() == AdjustmentType.PERCENTAGE
        ? adjustedPrice.add(adjustedPrice.multiply(roomRate.getAdjustmentRate().divide(BigDecimal.valueOf(100))))
        : adjustedPrice.add(Optional.ofNullable(roomRate.getAdjustmentRate()).orElse(BigDecimal.ZERO));
    return adjustedPrice.setScale(2, RoundingMode.HALF_UP);
  }

  public BigDecimal calculateAdjustedPrice(BigDecimal basePrice, List<PeakSeasonRate> applicableRates) {
    BigDecimal totalAdjustment = BigDecimal.ZERO;
    for (PeakSeasonRate rate : applicableRates) {
      if (rate.getAdjustmentType() == PeakSeasonRate.AdjustmentType.PERCENTAGE) {
        BigDecimal percentageAdjustment = basePrice.multiply(rate.getAdjustmentRate().divide(BigDecimal.valueOf(100)));
        totalAdjustment = totalAdjustment.add(percentageAdjustment);
      } else {
        totalAdjustment = totalAdjustment.add(rate.getAdjustmentRate());
      }
    }
    BigDecimal adjustedPrice = basePrice.add(totalAdjustment);
    return adjustedPrice.setScale(2, RoundingMode.HALF_UP);
  }
}