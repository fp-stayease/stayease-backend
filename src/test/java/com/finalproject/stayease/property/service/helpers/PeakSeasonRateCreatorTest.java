package com.finalproject.stayease.property.service.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.exceptions.utils.InvalidRequestException;
import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPeakSeasonRateRequestDTO;
import com.finalproject.stayease.property.repository.PeakSeasonRateRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PeakSeasonRateCreatorTest {

  @Mock
  private PeakSeasonRateRepository peakSeasonRateRepository;

  @InjectMocks
  private PeakSeasonRateCreator creator;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreateRate_Success() {
    Property property = new Property();
    SetPeakSeasonRateRequestDTO requestDTO = new SetPeakSeasonRateRequestDTO();
    requestDTO.setStartDate(LocalDate.now());
    requestDTO.setEndDate(LocalDate.now().plusDays(7));
    requestDTO.setAdjustmentRate(BigDecimal.valueOf(10));
    requestDTO.setAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);
    requestDTO.setReason("Test reason");

    when(peakSeasonRateRepository.save(any(PeakSeasonRate.class))).thenAnswer(i -> i.getArguments()[0]);

    PeakSeasonRate result = creator.createRate(property, requestDTO);

    assertNotNull(result);
    assertEquals(property, result.getProperty());
    assertEquals(requestDTO.getStartDate(), result.getStartDate());
    assertEquals(requestDTO.getEndDate(), result.getEndDate());
    assertEquals(requestDTO.getAdjustmentRate(), result.getAdjustmentRate());
    assertEquals(requestDTO.getAdjustmentType(), result.getAdjustmentType());
    assertEquals(requestDTO.getReason(), result.getReason());

    verify(peakSeasonRateRepository, times(1)).save(any(PeakSeasonRate.class));
  }

  @Test
  void testCreateRate_InvalidPercentage() {
    Property property = new Property();
    SetPeakSeasonRateRequestDTO requestDTO = new SetPeakSeasonRateRequestDTO();
    requestDTO.setAdjustmentRate(BigDecimal.valueOf(101));
    requestDTO.setAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);

    assertThrows(InvalidRequestException.class, () -> creator.createRate(property, requestDTO));
    verify(peakSeasonRateRepository, never()).save(any(PeakSeasonRate.class));
  }

  @Test
  void testUpdateRate_Success() {
    PeakSeasonRate existingRate = new PeakSeasonRate();
    existingRate.setStartDate(LocalDate.now().minusDays(1));
    existingRate.setEndDate(LocalDate.now().plusDays(6));
    existingRate.setAdjustmentRate(BigDecimal.valueOf(5));
    existingRate.setAdjustmentType(PeakSeasonRate.AdjustmentType.FIXED);
    existingRate.setReason("Old reason");

    SetPeakSeasonRateRequestDTO requestDTO = new SetPeakSeasonRateRequestDTO();
    requestDTO.setEndDate(LocalDate.now().plusDays(7));
    requestDTO.setAdjustmentRate(BigDecimal.valueOf(10));
    requestDTO.setReason("New reason");

    when(peakSeasonRateRepository.save(any(PeakSeasonRate.class))).thenAnswer(i -> i.getArguments()[0]);

    PeakSeasonRate result = creator.updateRate(existingRate, requestDTO);

    assertNotNull(result);
    assertEquals(existingRate.getStartDate(), result.getStartDate());
    assertEquals(requestDTO.getEndDate(), result.getEndDate());
    assertEquals(requestDTO.getAdjustmentRate(), result.getAdjustmentRate());
    assertEquals(existingRate.getAdjustmentType(), result.getAdjustmentType());
    assertEquals(requestDTO.getReason(), result.getReason());

    verify(peakSeasonRateRepository, times(1)).save(any(PeakSeasonRate.class));
  }
}
