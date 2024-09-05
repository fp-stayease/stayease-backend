package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPeakSeasonRateRequestDTO;
import com.finalproject.stayease.property.repository.PeakSeasonRateRepository;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.users.entity.Users;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;

@SpringBootTest
public class PeakSeasonRateServiceImplTest {
  @Mock
  private PeakSeasonRateRepository peakSeasonRateRepository;

  @Mock
  private PropertyService propertyService;

  @InjectMocks
  private PeakSeasonRateServiceImpl peakSeasonRateService;

  private Users tenant;
  private Property property;
  private SetPeakSeasonRateRequestDTO requestDTO;

  @BeforeEach
  void setUp() {
    tenant = new Users();
    tenant.setId(1L);

    property = new Property();
    property.setId(1L);
    property.setTenant(tenant);

    requestDTO = new SetPeakSeasonRateRequestDTO();
    requestDTO.setStartDate(LocalDate.of(2024, 7, 1));
    requestDTO.setEndDate(LocalDate.of(2024, 7, 31));
    requestDTO.setRateAdjustment(BigDecimal.valueOf(20));
    requestDTO.setAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);
  }

  @Test
  void testSetPeakSeasonRate_Success() {
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(peakSeasonRateRepository.findByStartDateAndEndDate(any(), any())).thenReturn(Optional.empty());

    PeakSeasonRate result = peakSeasonRateService.setPeakSeasonRate(tenant, 1L, requestDTO);

    assertNotNull(result);
    assertEquals(property, result.getProperty());
    assertEquals(requestDTO.getStartDate(), result.getStartDate());
    assertEquals(requestDTO.getEndDate(), result.getEndDate());
    assertEquals(requestDTO.getRateAdjustment(), result.getRateAdjustment());
    assertEquals(requestDTO.getAdjustmentType(), result.getAdjustmentType());

    verify(peakSeasonRateRepository).save(any(PeakSeasonRate.class));
  }

  @Test
  void testSetPeakSeasonRate_PropertyNotFound() {
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.empty());

    assertThrows(DataNotFoundException.class, () ->
        peakSeasonRateService.setPeakSeasonRate(tenant, 1L, requestDTO)
    );
  }

  @Test
  void testSetPeakSeasonRate_UnauthorizedTenant() {
    Users unauthorizedTenant = new Users();
    unauthorizedTenant.setId(2L);

    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));

    assertThrows(BadCredentialsException.class, () ->
        peakSeasonRateService.setPeakSeasonRate(unauthorizedTenant, 1L, requestDTO)
    );
  }

  @Test
  void testSetPeakSeasonRate_DuplicateEntry() {
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(peakSeasonRateRepository.findByStartDateAndEndDate(any(), any())).thenReturn(Optional.of(new PeakSeasonRate()));

    assertThrows(DuplicateEntryException.class, () ->
        peakSeasonRateService.setPeakSeasonRate(tenant, 1L, requestDTO)
    );
  }

  @Test
  void testApplyPeakSeasonRate_Percentage() {
    LocalDate date = LocalDate.of(2024, 7, 15);
    BigDecimal basePrice = BigDecimal.valueOf(100);

    PeakSeasonRate rate = new PeakSeasonRate();
    rate.setAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);
    rate.setRateAdjustment(BigDecimal.valueOf(20));

    when(peakSeasonRateRepository.findApplicablePeakSeasonRates(1L, date)).thenReturn(List.of(rate));

    BigDecimal result = peakSeasonRateService.applyPeakSeasonRate(1L, date, basePrice);

    assertEquals(BigDecimal.valueOf(120).setScale(2), result);
  }

  @Test
  void testApplyPeakSeasonRate_Fixed() {
    LocalDate date = LocalDate.of(2024, 7, 15);
    BigDecimal basePrice = BigDecimal.valueOf(100);

    PeakSeasonRate rate = new PeakSeasonRate();
    rate.setAdjustmentType(PeakSeasonRate.AdjustmentType.FIXED);
    rate.setRateAdjustment(BigDecimal.valueOf(50));

    when(peakSeasonRateRepository.findApplicablePeakSeasonRates(1L, date)).thenReturn(Arrays.asList(rate));

    BigDecimal result = peakSeasonRateService.applyPeakSeasonRate(1L, date, basePrice);

    assertEquals(BigDecimal.valueOf(150).setScale(2), result);
  }

  @Test
  void testApplyPeakSeasonRate_MultipleRates() {
    LocalDate date = LocalDate.of(2024, 7, 15);
    BigDecimal basePrice = BigDecimal.valueOf(100);

    PeakSeasonRate rate1 = new PeakSeasonRate();
    rate1.setAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);
    rate1.setRateAdjustment(BigDecimal.valueOf(20));

    PeakSeasonRate rate2 = new PeakSeasonRate();
    rate2.setAdjustmentType(PeakSeasonRate.AdjustmentType.FIXED);
    rate2.setRateAdjustment(BigDecimal.valueOf(30));

    when(peakSeasonRateRepository.findApplicablePeakSeasonRates(1L, date)).thenReturn(Arrays.asList(rate1, rate2));

    BigDecimal result = peakSeasonRateService.applyPeakSeasonRate(1L, date, basePrice);

    assertEquals(BigDecimal.valueOf(150).setScale(2), result);
  }
}
