package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.exceptions.properties.PropertyNotFoundException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.createRequests.CreatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyListingDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdatePropertyRequestDTO;
import com.finalproject.stayease.property.repository.PropertyRepository;
import com.finalproject.stayease.property.service.helpers.PropertyServiceHelper;
import com.finalproject.stayease.users.entity.Users;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PropertyServiceImplTest {

  @Mock
  private PropertyRepository propertyRepository;

  @Mock
  private PropertyServiceHelper propertyServiceHelper;

  @InjectMocks
  private PropertyServiceImpl propertyService;

  private Users tenant;
  private Property property;
  private CreatePropertyRequestDTO createDTO;
  private UpdatePropertyRequestDTO updateDTO;

  @BeforeEach
  void setUp() {
    tenant = new Users();
    tenant.setId(1L);
    tenant.setUserType(Users.UserType.TENANT);

    property = new Property();
    property.setId(1L);
    property.setTenant(tenant);

    createDTO = new CreatePropertyRequestDTO();
    updateDTO = new UpdatePropertyRequestDTO();
  }

  @Test
  void findAll_Success() {
    when(propertyRepository.findAll()).thenReturn(Collections.singletonList(property));
    List<Property> result = propertyService.findAll();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  void findAll_NoPropertiesFound() {
    when(propertyRepository.findAll()).thenReturn(Collections.emptyList());
    assertThrows(PropertyNotFoundException.class, () -> propertyService.findAll());
  }

  @Test
  void findAllByTenant_Success() {
    when(propertyRepository.findByTenantAndDeletedAtIsNull(tenant)).thenReturn(Collections.singletonList(property));
    List<Property> result = propertyService.findAllByTenant(tenant);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  void findAllByTenant_NoPropertiesFound() {
    when(propertyRepository.findByTenantAndDeletedAtIsNull(tenant)).thenReturn(Collections.emptyList());
    assertThrows(PropertyNotFoundException.class, () -> propertyService.findAllByTenant(tenant));
  }

  @Test
  void createProperty_Success() {
    when(propertyServiceHelper.toPropertyEntity(tenant, createDTO)).thenReturn(property);
    Property result = propertyService.createProperty(tenant, createDTO);
    assertNotNull(result);
    assertEquals(property, result);
  }

  @Test
  void updateProperty_Success() {
    when(propertyServiceHelper.checkIfValid(tenant, 1L)).thenReturn(property);
    when(propertyServiceHelper.update(property, updateDTO)).thenReturn(property);
    Property result = propertyService.updateProperty(tenant, 1L, updateDTO);
    assertNotNull(result);
    assertEquals(property, result);
  }

  @Test
  void deleteProperty_Success() {
    when(propertyServiceHelper.checkIfValid(tenant, 1L)).thenReturn(property);
    when(propertyRepository.save(any(Property.class))).thenReturn(property);
    Property result = propertyService.deleteProperty(tenant, 1L);
    assertNotNull(result);
    assertNotNull(result.getDeletedAt());
  }

  @Test
  void hardDeleteStaleProperties_Success() {
    when(propertyRepository.deleteAllDeletedPropertiesOlderThan(any(Instant.class))).thenReturn(1);
    int result = propertyService.hardDeleteStaleProperties(Instant.now());
    assertEquals(1, result);
  }

  @Test
  void getAllAvailablePropertiesOnDate_Success() {
    LocalDate date = LocalDate.now().plusDays(1);
    when(propertyRepository.findAvailablePropertiesOnDate(date)).thenReturn(Collections.singletonList(property));
    List<Property> result = propertyService.getAllAvailablePropertiesOnDate(date);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  void findLowestRoomRate_Success() {
    LocalDate date = LocalDate.now().plusDays(1);
    RoomPriceRateDTO rateDTO = new RoomPriceRateDTO();
    when(propertyRepository.findAvailableRoomRates(1L, date)).thenReturn(Collections.singletonList(rateDTO));
    RoomPriceRateDTO result = propertyService.findLowestRoomRate(1L, date);
    assertNotNull(result);
  }

  @Test
  void findAvailableProperties_Success() {
    LocalDate startDate = LocalDate.now().plusDays(1);
    LocalDate endDate = LocalDate.now().plusDays(2);
    when(propertyRepository.findAvailableProperties(eq(startDate), eq(endDate), any(), any(), any(), any(), any(), any()))
        .thenReturn(Collections.singletonList(new PropertyListingDTO()));
    List<PropertyListingDTO> result = propertyService.findAvailableProperties(startDate, endDate, "City", "Category", "Search", BigDecimal.ONE, BigDecimal.TEN, 2);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  void isTenantPropertyOwner_Success() {
    when(propertyServiceHelper.checkIfValid(tenant, 1L)).thenReturn(property);
    boolean result = propertyService.isTenantPropertyOwner(tenant, 1L);
    assertTrue(result);
  }

  @Test
  void isTenantPropertyOwner_NotOwner() {
    when(propertyServiceHelper.checkIfValid(tenant, 1L)).thenThrow(new RuntimeException());
    boolean result = propertyService.isTenantPropertyOwner(tenant, 1L);
    assertFalse(result);
  }
}