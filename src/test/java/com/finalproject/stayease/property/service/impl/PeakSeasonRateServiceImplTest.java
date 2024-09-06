package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.PeakSeasonRate.AdjustmentType;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPeakSeasonRateRequestDTO;
import com.finalproject.stayease.property.repository.PeakSeasonRateRepository;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.users.entity.Users;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
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
    Instant bookingTime = Instant.now();

    PeakSeasonRate rate = new PeakSeasonRate();
    rate.setAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);
    rate.setRateAdjustment(BigDecimal.valueOf(20));

    when(peakSeasonRateRepository.findValidRatesByPropertyAndDate(1L, date, bookingTime, Instant.MAX)).thenReturn(List.of(rate));

    BigDecimal result = peakSeasonRateService.applyPeakSeasonRate(1L, date, basePrice, bookingTime);

    assertEquals(BigDecimal.valueOf(120).setScale(2), result);
  }

  @Test
  void testApplyPeakSeasonRate_Fixed() {
    LocalDate date = LocalDate.of(2024, 7, 15);
    BigDecimal basePrice = BigDecimal.valueOf(100);
    Instant bookingTime = Instant.now();

    PeakSeasonRate rate = new PeakSeasonRate();
    rate.setAdjustmentType(PeakSeasonRate.AdjustmentType.FIXED);
    rate.setRateAdjustment(BigDecimal.valueOf(50));

    when(peakSeasonRateRepository.findValidRatesByPropertyAndDate(1L, date, bookingTime, Instant.MAX)).thenReturn(List.of(rate));

    BigDecimal result = peakSeasonRateService.applyPeakSeasonRate(1L, date, basePrice, bookingTime);

    assertEquals(BigDecimal.valueOf(150).setScale(2), result);
  }

  @Test
  void testApplyPeakSeasonRate_MultipleRates() {
    LocalDate date = LocalDate.of(2024, 7, 15);
    BigDecimal basePrice = BigDecimal.valueOf(100);
    Instant bookingTime = Instant.now();

    PeakSeasonRate rate1 = new PeakSeasonRate();
    rate1.setAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);
    rate1.setRateAdjustment(BigDecimal.valueOf(20));

    PeakSeasonRate rate2 = new PeakSeasonRate();
    rate2.setAdjustmentType(PeakSeasonRate.AdjustmentType.FIXED);
    rate2.setRateAdjustment(BigDecimal.valueOf(30));

    when(peakSeasonRateRepository.findValidRatesByPropertyAndDate(1L, date, bookingTime, Instant.MAX)).thenReturn(List.of(rate1, rate2));

    BigDecimal result = peakSeasonRateService.applyPeakSeasonRate(1L, date, basePrice, bookingTime);

    assertEquals(BigDecimal.valueOf(150).setScale(2), result);
  }

  @Test
  void updatePeakSeasonRate_Success() {
    // Arrange
    Users tenant = new Users();
    Long propertyId = 1L;
    Long rateId = 1L;
    SetPeakSeasonRateRequestDTO requestDTO = new SetPeakSeasonRateRequestDTO();
    requestDTO.setEndDate(LocalDate.now().plusDays(7));
    requestDTO.setRateAdjustment(BigDecimal.valueOf(10));
    requestDTO.setAdjustmentType(AdjustmentType.PERCENTAGE);

    Property property = new Property();
    property.setTenant(tenant);

    PeakSeasonRate existingRate = new PeakSeasonRate();
    existingRate.setStartDate(LocalDate.now());

    when(propertyService.findPropertyById(propertyId)).thenReturn(Optional.of(property));
    when(peakSeasonRateRepository.findById(rateId)).thenReturn(Optional.of(existingRate));
    when(peakSeasonRateRepository.save(any(PeakSeasonRate.class))).thenAnswer(i -> i.getArguments()[0]);

    // Act
    PeakSeasonRate result = peakSeasonRateService.updatePeakSeasonRate(tenant, propertyId, rateId, requestDTO);

    // Assert
    assertNotNull(result);
    assertEquals(requestDTO.getEndDate(), result.getEndDate());
    assertEquals(requestDTO.getRateAdjustment(), result.getRateAdjustment());
    assertEquals(requestDTO.getAdjustmentType(), result.getAdjustmentType());
    assertEquals(existingRate.getStartDate(), result.getStartDate());
    verify(peakSeasonRateRepository, times(2)).save(any(PeakSeasonRate.class));
  }

  @Test
  void updatePeakSeasonRate_PropertyNotFound() {
    // Arrange
    Users tenant = new Users();
    Long propertyId = 1L;
    Long rateId = 1L;
    SetPeakSeasonRateRequestDTO requestDTO = new SetPeakSeasonRateRequestDTO();

    when(propertyService.findPropertyById(propertyId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(DataNotFoundException.class, () ->
        peakSeasonRateService.updatePeakSeasonRate(tenant, propertyId, rateId, requestDTO));
  }

  @Test
  void updatePeakSeasonRate_UnauthorizedTenant() {
    // Arrange
    Users tenant = new Users();
    Users otherTenant = new Users();
    Long propertyId = 1L;
    Long rateId = 1L;
    SetPeakSeasonRateRequestDTO requestDTO = new SetPeakSeasonRateRequestDTO();

    Property property = new Property();
    property.setTenant(otherTenant);

    when(propertyService.findPropertyById(propertyId)).thenReturn(Optional.of(property));

    // Act & Assert
    assertThrows(BadCredentialsException.class, () ->
        peakSeasonRateService.updatePeakSeasonRate(tenant, propertyId, rateId, requestDTO));
  }

  @Test
  void updatePeakSeasonRate_RateNotFound() {
    // Arrange
    Users tenant = new Users();
    Long propertyId = 1L;
    Long rateId = 1L;
    SetPeakSeasonRateRequestDTO requestDTO = new SetPeakSeasonRateRequestDTO();

    Property property = new Property();
    property.setTenant(tenant);

    when(propertyService.findPropertyById(propertyId)).thenReturn(Optional.of(property));
    when(peakSeasonRateRepository.findById(rateId)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(DataNotFoundException.class, () ->
        peakSeasonRateService.updatePeakSeasonRate(tenant, propertyId, rateId, requestDTO));
  }

  @Test
  void applyPeakSeasonRate_NoApplicableRates() {
    // Arrange
    Long propertyId = 1L;
    LocalDate date = LocalDate.of(2023, 7, 1);
    BigDecimal basePrice = new BigDecimal("100.00");
    Instant bookingTime = Instant.now();

    when(peakSeasonRateRepository.findValidRatesByPropertyAndDate(eq(propertyId), eq(date), any(Instant.class), any(Instant.class)))
        .thenReturn(Collections.emptyList());

    // Act
    BigDecimal result = peakSeasonRateService.applyPeakSeasonRate(propertyId, date, basePrice, bookingTime);

    // Assert
    assertEquals(basePrice, result);
  }

  @Test
  void applyPeakSeasonRate_SinglePercentageAdjustment() {
    // Arrange
    Long propertyId = 1L;
    LocalDate date = LocalDate.of(2023, 7, 1);
    BigDecimal basePrice = new BigDecimal("100.00");
    Instant bookingTime = Instant.now();

    PeakSeasonRate rate = new PeakSeasonRate();
    rate.setAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);
    rate.setRateAdjustment(new BigDecimal("10.00")); // 10% increase

    when(peakSeasonRateRepository.findValidRatesByPropertyAndDate(eq(propertyId), eq(date), any(Instant.class), any(Instant.class)))
        .thenReturn(Collections.singletonList(rate));

    // Act
    BigDecimal result = peakSeasonRateService.applyPeakSeasonRate(propertyId, date, basePrice, bookingTime);

    // Assert
    assertEquals(new BigDecimal("110.00"), result);
  }

  @Test
  void applyPeakSeasonRate_SingleFixedAdjustment() {
    // Arrange
    Long propertyId = 1L;
    LocalDate date = LocalDate.of(2023, 7, 1);
    BigDecimal basePrice = new BigDecimal("100.00");
    Instant bookingTime = Instant.now();

    PeakSeasonRate rate = new PeakSeasonRate();
    rate.setAdjustmentType(PeakSeasonRate.AdjustmentType.FIXED);
    rate.setRateAdjustment(new BigDecimal("20.00")); // $20 increase

    when(peakSeasonRateRepository.findValidRatesByPropertyAndDate(eq(propertyId), eq(date), any(Instant.class), any(Instant.class)))
        .thenReturn(Collections.singletonList(rate));

    // Act
    BigDecimal result = peakSeasonRateService.applyPeakSeasonRate(propertyId, date, basePrice, bookingTime);

    // Assert
    assertEquals(new BigDecimal("120.00"), result);
  }

  @Test
  void applyPeakSeasonRate_MultipleAdjustments() {
    // Arrange
    Long propertyId = 1L;
    LocalDate date = LocalDate.of(2023, 7, 1);
    BigDecimal basePrice = new BigDecimal("100.00");
    Instant bookingTime = Instant.now();

    PeakSeasonRate rate1 = new PeakSeasonRate();
    rate1.setAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);
    rate1.setRateAdjustment(new BigDecimal("10.00")); // 10% increase

    PeakSeasonRate rate2 = new PeakSeasonRate();
    rate2.setAdjustmentType(PeakSeasonRate.AdjustmentType.FIXED);
    rate2.setRateAdjustment(new BigDecimal("20.00")); // $20 increase

    when(peakSeasonRateRepository.findValidRatesByPropertyAndDate(eq(propertyId), eq(date), any(Instant.class), any(Instant.class)))
        .thenReturn(Arrays.asList(rate1, rate2));

    // Act
    BigDecimal result = peakSeasonRateService.applyPeakSeasonRate(propertyId, date, basePrice, bookingTime);

    // Assert
    // Expected: 100 + (100 * 10%) + 20 = 130
    assertEquals(new BigDecimal("130.00"), result);
  }

  @Test
  void applyPeakSeasonRate_RoundingCheck() {
    // Arrange
    Long propertyId = 1L;
    LocalDate date = LocalDate.of(2023, 7, 1);
    BigDecimal basePrice = new BigDecimal("99.99");
    Instant bookingTime = Instant.now();

    PeakSeasonRate rate = new PeakSeasonRate();
    rate.setAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);
    rate.setRateAdjustment(new BigDecimal("5.5")); // 5.5% increase

    when(peakSeasonRateRepository.findValidRatesByPropertyAndDate(eq(propertyId), eq(date), any(Instant.class), any(Instant.class)))
        .thenReturn(Collections.singletonList(rate));

    // Act
    BigDecimal result =
        peakSeasonRateService.applyPeakSeasonRate(propertyId, date, basePrice, bookingTime);

    // Assert
    // Expected: 99.99 + (99.99 * 5.5%) = 105.49 (rounded to 2 decimal places)
    assertEquals(new BigDecimal("105.49"), result);
  }
}
