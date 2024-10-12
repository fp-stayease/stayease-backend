package com.finalproject.stayease.property.service.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PeakSeasonRateCalculatorTest {

  @InjectMocks
  private PeakSeasonRateCalculator calculator;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testApplyPeakSeasonRate_Percentage() {
    RoomPriceRateDTO roomRate = new RoomPriceRateDTO();
    roomRate.setBasePrice(BigDecimal.valueOf(100));
    roomRate.setAdjustmentRate(BigDecimal.valueOf(10));
    roomRate.setAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);

    BigDecimal result = calculator.applyPeakSeasonRate(roomRate);
    assertEquals(BigDecimal.valueOf(110.00).setScale(2), result);
  }

  @Test
  void testApplyPeakSeasonRate_Fixed() {
    RoomPriceRateDTO roomRate = new RoomPriceRateDTO();
    roomRate.setBasePrice(BigDecimal.valueOf(100));
    roomRate.setAdjustmentRate(BigDecimal.valueOf(20));
    roomRate.setAdjustmentType(PeakSeasonRate.AdjustmentType.FIXED);

    BigDecimal result = calculator.applyPeakSeasonRate(roomRate);
    assertEquals(BigDecimal.valueOf(120.00).setScale(2), result);
  }

  @Test
  void testCalculateAdjustedPrice_MultipleRates() {
    BigDecimal basePrice = BigDecimal.valueOf(100);
    List<PeakSeasonRate> rates = Arrays.asList(
        createRate(BigDecimal.valueOf(10), PeakSeasonRate.AdjustmentType.PERCENTAGE),
        createRate(BigDecimal.valueOf(5), PeakSeasonRate.AdjustmentType.FIXED)
    );

    BigDecimal result = calculator.calculateAdjustedPrice(basePrice, rates);
    assertEquals(BigDecimal.valueOf(115.00).setScale(2), result);
  }

  private PeakSeasonRate createRate(BigDecimal rate, PeakSeasonRate.AdjustmentType type) {
    PeakSeasonRate peakSeasonRate = new PeakSeasonRate();
    peakSeasonRate.setAdjustmentRate(rate);
    peakSeasonRate.setAdjustmentType(type);
    return peakSeasonRate;
  }
}
