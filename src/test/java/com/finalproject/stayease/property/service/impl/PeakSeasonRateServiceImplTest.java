package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.createRequests.SetPeakSeasonRateRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.DailyPriceDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import com.finalproject.stayease.property.repository.PeakSeasonRateRepository;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.users.entity.Users;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class PeakSeasonRateServiceImplTest {

  @Mock
  private PeakSeasonRateRepository peakSeasonRateRepository;

  @Mock
  private PropertyService propertyService;

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
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(peakSeasonRateRepository.save(any(PeakSeasonRate.class))).thenReturn(peakSeasonRate);

    PeakSeasonRate result = peakSeasonRateService.setPeakSeasonRate(1L, requestDTO);

    assertNotNull(result);
    assertEquals(peakSeasonRate.getProperty().getId(), result.getProperty().getId());
    verify(peakSeasonRateRepository).save(any(PeakSeasonRate.class));
  }

  @Test
  void setPeakSeasonRate_PropertyNotFound() {
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.empty());

    assertThrows(DataNotFoundException.class, () -> peakSeasonRateService.setPeakSeasonRate(1L, requestDTO));
  }

  @Test
  void setPeakSeasonRate_DuplicateEntry() {
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(peakSeasonRateRepository.existsConflictingRate(anyLong(), any(LocalDate.class), any(LocalDate.class)))
        .thenReturn(true);

    assertThrows(DuplicateEntryException.class, () -> peakSeasonRateService.setPeakSeasonRate(tenant, 1L, requestDTO));
  }

  @Test
  void updatePeakSeasonRate_Success() {
    when(peakSeasonRateRepository.findById(1L)).thenReturn(Optional.of(peakSeasonRate));
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(peakSeasonRateRepository.save(any(PeakSeasonRate.class))).thenReturn(peakSeasonRate);

    PeakSeasonRate result = peakSeasonRateService.updatePeakSeasonRate(tenant, 1L, requestDTO);

    assertNotNull(result);
    assertEquals(peakSeasonRate.getId(), result.getId());
    verify(peakSeasonRateRepository).save(any(PeakSeasonRate.class));
  }

  @Test
  void updatePeakSeasonRate_RateNotFound() {
    when(peakSeasonRateRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(DataNotFoundException.class, () -> peakSeasonRateService.updatePeakSeasonRate(tenant, 1L, requestDTO));
  }

  @Test
  void updatePeakSeasonRate_NotPropertyOwner() {
    Users otherTenant = new Users();
    otherTenant.setId(2L);
    property.setTenant(otherTenant);

    when(peakSeasonRateRepository.findById(1L)).thenReturn(Optional.of(peakSeasonRate));
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));

    assertThrows(BadCredentialsException.class, () -> peakSeasonRateService.updatePeakSeasonRate(tenant, 1L, requestDTO));
  }

  @Test
  void removePeakSeasonRate_Success() {
    when(peakSeasonRateRepository.findById(1L)).thenReturn(Optional.of(peakSeasonRate));

    peakSeasonRateService.removePeakSeasonRate(1L);

    verify(peakSeasonRateRepository).save(peakSeasonRate);
    assertNotNull(peakSeasonRate.getDeletedAt());
  }

  @Test
  void removePeakSeasonRate_RateNotFound() {
    when(peakSeasonRateRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(DataNotFoundException.class, () -> peakSeasonRateService.removePeakSeasonRate(1L));
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

    List<RoomAdjustedRatesDTO> result = peakSeasonRateService.findAvailableRoomRates(1L, LocalDate.now());

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    assertEquals(BigDecimal.valueOf(110).setScale(2, RoundingMode.HALF_UP), result.getFirst().getAdjustedPrice());
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

    List<DailyPriceDTO> result = peakSeasonRateService.findLowestDailyRoomRates(1L, LocalDate.now(), LocalDate.now().plusDays(3));

    assertFalse(result.isEmpty());
    assertEquals(3, result.size());
    assertEquals(BigDecimal.valueOf(110).setScale(2, RoundingMode.HALF_UP), result.getFirst().getLowestPrice());
    assertTrue(result.getFirst().isHasAdjustment());
  }


  @Test
  void applyPeakSeasonRate_Percentage() {
    RoomPriceRateDTO roomRate = new RoomPriceRateDTO();
    roomRate.setBasePrice(BigDecimal.valueOf(100));
    roomRate.setAdjustmentRate(BigDecimal.valueOf(10));
    roomRate.setAdjustmentType(PeakSeasonRate.AdjustmentType.PERCENTAGE);

    BigDecimal result = peakSeasonRateService.applyPeakSeasonRate(roomRate);

    assertEquals(BigDecimal.valueOf(110).setScale(2, RoundingMode.HALF_UP), result);
  }

  @Test
  void applyPeakSeasonRate_FixedAmount() {
    RoomPriceRateDTO roomRate = new RoomPriceRateDTO();
    roomRate.setBasePrice(BigDecimal.valueOf(100));
    roomRate.setAdjustmentRate(BigDecimal.valueOf(10));
    roomRate.setAdjustmentType(PeakSeasonRate.AdjustmentType.FIXED);

    BigDecimal result = peakSeasonRateService.applyPeakSeasonRate(roomRate);

    assertEquals(BigDecimal.valueOf(110).setScale(2, RoundingMode.HALF_UP), result);
  }
}