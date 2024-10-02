package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyAvailableOnDateDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyListingDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.RoomService;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import java.math.BigDecimal;
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
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
public class PropertyListingServiceImplTest {

  @Mock
  private PropertyService propertyService;

  @Mock
  private PeakSeasonRateService peakSeasonRateService;

  @Mock
  private RoomService roomService;

  @InjectMocks
  private PropertyListingServiceImpl propertyListingService;

  private Property property;
  private PropertyListingDTO propertyListingDTO;
  private RoomAdjustedRatesDTO roomAdjustedRatesDTO;

  @BeforeEach
  void setUp() {
    Users tenant = new Users();
    tenant.setId(1L);
    tenant.setUserType(Users.UserType.TENANT);

    TenantInfo tenantInfo = new TenantInfo();
    tenantInfo.setId(1L);
    tenantInfo.setUser(tenant);
    tenantInfo.setBusinessName("Business Name");
    tenant.setTenantInfo(tenantInfo);

    PropertyCategory category = new PropertyCategory();
    category.setId(1L);
    category.setName("Apartment");

    property = new Property();
    property.setId(1L);
    property.setName("Test Property");
    property.setTenant(tenant);
    property.setCategory(category);

    propertyListingDTO = new PropertyListingDTO();
    propertyListingDTO.setPropertyId(1L);
    propertyListingDTO.setPropertyName("Test Property");
    propertyListingDTO.setLowestBasePrice(BigDecimal.valueOf(100));

    roomAdjustedRatesDTO = new RoomAdjustedRatesDTO();
    roomAdjustedRatesDTO.setRoomId(1L);
    roomAdjustedRatesDTO.setBasePrice(BigDecimal.valueOf(100));
    roomAdjustedRatesDTO.setAdjustedPrice(BigDecimal.valueOf(120));
  }

  @Test
  void findAvailableProperties_Success() {
    when(propertyService.findAvailableProperties(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Collections.singletonList(propertyListingDTO));
    when(peakSeasonRateService.applyPeakSeasonRate(anyLong(), any(), any(), any()))
        .thenReturn(BigDecimal.valueOf(120));

    Page<PropertyListingDTO> result = propertyListingService.findAvailableProperties(
        LocalDate.now(), LocalDate.now().plusDays(1), "City", 1L,
        "searchTerm", BigDecimal.valueOf(50), BigDecimal.valueOf(200),
        0, 10, "price", "ASC");

    assertNotNull(result);
    assertFalse(result.getContent().isEmpty());
    assertEquals(1, result.getContent().size());
    assertEquals(BigDecimal.valueOf(120), result.getContent().getFirst().getLowestAdjustedPrice());
  }

  @Test
  void findAvailablePropertyOnDate_Success() {
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.of(property));
    when(peakSeasonRateService.findAvailableRoomRates(1L, LocalDate.now()))
        .thenReturn(Collections.singletonList(roomAdjustedRatesDTO));
    when(roomService.getUnavailableRoomsByPropertyIdAndDate(1L, LocalDate.now()))
        .thenReturn(Collections.emptyList());

    PropertyAvailableOnDateDTO result = propertyListingService.findAvailablePropertyOnDate(1L, LocalDate.now());

    assertNotNull(result);
    assertEquals(property.getName(), result.getPropertyName());
    assertFalse(result.getRooms().isEmpty());
    assertTrue(result.getUnavailableRooms().isEmpty());
  }

  @Test
  void findAvailablePropertyOnDate_PropertyNotFound() {
    when(propertyService.findPropertyById(1L)).thenReturn(Optional.empty());

    assertThrows(DataNotFoundException.class,
        () -> propertyListingService.findAvailablePropertyOnDate(1L, LocalDate.now()));
  }

  @Test
  void findPropertiesWithLowestRoomRate_Success() {
    when(propertyService.getAllAvailablePropertiesOnDate(any())).thenReturn(Collections.singletonList(property));
    when(peakSeasonRateService.findAvailableRoomRates(anyLong(), any()))
        .thenReturn(Collections.singletonList(roomAdjustedRatesDTO));

    List<PropertyListingDTO> result = propertyListingService.findPropertiesWithLowestRoomRate(LocalDate.now());

    assertNotNull(result);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  void findPropertiesWithLowestRoomRate_NoRoomRates() {
    when(propertyService.getAllAvailablePropertiesOnDate(any())).thenReturn(Collections.singletonList(property));
    when(peakSeasonRateService.findAvailableRoomRates(anyLong(), any()))
        .thenReturn(Collections.emptyList());

    assertThrows(DataNotFoundException.class,
        () -> propertyListingService.findPropertiesWithLowestRoomRate(LocalDate.now()));
  }

  @Test
  void validateDate_InvalidDate() {
    assertThrows(IllegalArgumentException.class,
        () -> propertyListingService.findAvailablePropertyOnDate(1L, LocalDate.now().minusDays(1)));
  }

  @Test
  void validateDate_InvalidDateRange() {
    assertThrows(IllegalArgumentException.class,
        () -> propertyListingService.findAvailableProperties(
            LocalDate.now().plusDays(1), LocalDate.now(), null, null,
            null, null, null, 0, 10, "price", "ASC"));
  }
}