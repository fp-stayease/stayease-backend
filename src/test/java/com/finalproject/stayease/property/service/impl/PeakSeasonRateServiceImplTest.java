package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.finalproject.stayease.exceptions.properties.PropertyNotFoundException;
import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPeakSeasonRateRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.DailyPriceDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import com.finalproject.stayease.property.repository.PeakSeasonRateRepository;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.helpers.PeakSeasonRateCalculator;
import com.finalproject.stayease.property.service.helpers.PeakSeasonRateCreator;
import com.finalproject.stayease.property.service.helpers.PeakSeasonRateValidator;
import com.finalproject.stayease.users.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class PeakSeasonRateServiceImplTest {

  @Mock
  private PeakSeasonRateRepository peakSeasonRateRepository;
  @Mock
  private PropertyService propertyService;
  @Mock
  private PeakSeasonRateValidator validator;
  @Mock
  private PeakSeasonRateCreator creator;
  @Mock
  private PeakSeasonRateCalculator calculator;

  @InjectMocks
  private PeakSeasonRateServiceImpl peakSeasonRateService;

  private Users tenant;
  private Property property;
  private SetPeakSeasonRateRequestDTO requestDTO;
  private PeakSeasonRate peakSeasonRate;

  @BeforeEach
  void setUp() {
    tenant = new Users();
    tenant.setId(1L);

    property = new Property();
    property.setId(1L);
    property.setTenant(tenant);

    requestDTO = new SetPeakSeasonRateRequestDTO();
    requestDTO.setStartDate(LocalDate.now());
    requestDTO.setEndDate(LocalDate.now().plusDays(7));
    requestDTO.setAdjustmentRate(BigDecimal.valueOf(10));
    requestDTO.setAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);
    requestDTO.setReason("Holiday Season");

    peakSeasonRate = new PeakSeasonRate();
    peakSeasonRate.setId(1L);
    peakSeasonRate.setProperty(property);
    peakSeasonRate.setStartDate(requestDTO.getStartDate());
    peakSeasonRate.setEndDate(requestDTO.getEndDate());
    peakSeasonRate.setAdjustmentRate(requestDTO.getAdjustmentRate());
    peakSeasonRate.setAdjustmentType(requestDTO.getAdjustmentType());
    peakSeasonRate.setReason(requestDTO.getReason());
  }

  @Test
  void setPeakSeasonRate_Success() {
    when(validator.checkAndRetrieveProperty(1L)).thenReturn(property);
    when(creator.createRate(any(Property.class), any(SetPeakSeasonRateRequestDTO.class))).thenReturn(peakSeasonRate);

    PeakSeasonRate result = peakSeasonRateService.setPeakSeasonRate(1L, requestDTO);

    assertNotNull(result);
    assertEquals(peakSeasonRate.getProperty().getId(), result.getProperty().getId());
    verify(creator).createRate(any(Property.class), any(SetPeakSeasonRateRequestDTO.class));
  }

  @Test
  void setPeakSeasonRate_PropertyNotFound() {
    when(validator.checkAndRetrieveProperty(1L)).thenThrow(new PropertyNotFoundException("Property not found"));

    assertThrows(PropertyNotFoundException.class, () -> peakSeasonRateService.setPeakSeasonRate(1L, requestDTO));
  }

  @Test
  void setPeakSeasonRate_ForTenant_Success() {
    when(validator.validatePropertyOwnership(tenant, 1L)).thenReturn(property);
    when(creator.createRate(any(Property.class), any(SetPeakSeasonRateRequestDTO.class))).thenReturn(peakSeasonRate);

    PeakSeasonRate result = peakSeasonRateService.setPeakSeasonRate(tenant, 1L, requestDTO);

    assertNotNull(result);
    assertEquals(peakSeasonRate.getProperty().getId(), result.getProperty().getId());
    verify(validator).validateRateDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class));
    verify(creator).createRate(any(Property.class), any(SetPeakSeasonRateRequestDTO.class));
  }

  @Test
  void updatePeakSeasonRate_Success() {
    when(validator.checkAndRetrievePeakSeasonRate(1L)).thenReturn(peakSeasonRate);
    when(validator.validatePropertyOwnership(tenant, 1L)).thenReturn(property);
    when(creator.updateRate(any(PeakSeasonRate.class), any(SetPeakSeasonRateRequestDTO.class))).thenReturn(peakSeasonRate);

    PeakSeasonRate result = peakSeasonRateService.updatePeakSeasonRate(tenant, 1L, requestDTO);

    assertNotNull(result);
    assertEquals(peakSeasonRate.getId(), result.getId());
    verify(creator).updateRate(any(PeakSeasonRate.class), any(SetPeakSeasonRateRequestDTO.class));
  }

  @Test
  void removePeakSeasonRate_Success() {
    when(validator.checkAndRetrievePeakSeasonRate(1L)).thenReturn(peakSeasonRate);

    peakSeasonRateService.removePeakSeasonRate(1L);

    verify(peakSeasonRateRepository).save(peakSeasonRate);
    assertNotNull(peakSeasonRate.getDeletedAt());
  }

  @Test
  void getTenantCurrentRates_Success() {
    List<Property> properties = Collections.singletonList(property);
    List<PeakSeasonRate> rates = Collections.singletonList(peakSeasonRate);

    when(propertyService.findAllByTenant(tenant)).thenReturn(properties);
    when(peakSeasonRateRepository.findByPropertyAndEndDateAfterAndDeletedAtIsNull(eq(property), any(LocalDate.class)))
        .thenReturn(rates);

    List<PeakSeasonRate> result = peakSeasonRateService.getTenantCurrentRates(tenant);

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(peakSeasonRate, result.getFirst());
  }

  @Test
  void findAvailableRoomRates_Success() {
    RoomPriceRateDTO roomPriceRate = new RoomPriceRateDTO();
    roomPriceRate.setPropertyId(1L);
    roomPriceRate.setRoomId(1L);
    roomPriceRate.setBasePrice(BigDecimal.valueOf(100));

    when(propertyService.findAvailableRoomRates(1L, LocalDate.now())).thenReturn(List.of(roomPriceRate));
    when(peakSeasonRateRepository.findValidRatesByPropertyAndDate(eq(1L), any(LocalDate.class), any(Instant.class), any(Instant.class)))
        .thenReturn(Collections.singletonList(peakSeasonRate));
    when(calculator.calculateAdjustedPrice(any(BigDecimal.class), anyList())).thenReturn(BigDecimal.valueOf(110));

    List<RoomAdjustedRatesDTO> result = peakSeasonRateService.findAvailableRoomRates(1L, LocalDate.now());

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(BigDecimal.valueOf(110), result.getFirst().getAdjustedPrice());
  }

  @Test
  void findLowestDailyRoomRates_Success() {
    RoomPriceRateDTO roomPriceRate = new RoomPriceRateDTO();
    roomPriceRate.setPropertyId(1L);
    roomPriceRate.setRoomId(1L);
    roomPriceRate.setBasePrice(BigDecimal.valueOf(100));
    roomPriceRate.setAdjustmentRate(BigDecimal.valueOf(10));
    roomPriceRate.setAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);

    when(propertyService.findLowestRoomRate(eq(1L), any(LocalDate.class))).thenReturn(roomPriceRate);
    when(calculator.applyPeakSeasonRate(any(RoomPriceRateDTO.class))).thenReturn(BigDecimal.valueOf(110));

    List<DailyPriceDTO> result = peakSeasonRateService.findLowestDailyRoomRates(1L, LocalDate.now(), LocalDate.now().plusDays(3));

    assertFalse(result.isEmpty());
    assertEquals(3, result.size());
    assertEquals(BigDecimal.valueOf(110), result.getFirst().getLowestPrice());
    assertTrue(result.getFirst().isHasAdjustment());
  }

  @Test
  void applyPeakSeasonRate_Success() {
    RoomPriceRateDTO roomRate = new RoomPriceRateDTO();
    roomRate.setBasePrice(BigDecimal.valueOf(100));
    roomRate.setAdjustmentRate(BigDecimal.valueOf(10));
    roomRate.setAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);

    when(calculator.applyPeakSeasonRate(roomRate)).thenReturn(BigDecimal.valueOf(110));

    BigDecimal result = peakSeasonRateService.applyPeakSeasonRate(roomRate);

    assertEquals(BigDecimal.valueOf(110), result);
    verify(calculator).applyPeakSeasonRate(roomRate);
  }
}