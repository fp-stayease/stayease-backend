package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.exceptions.InvalidRequestException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.createRequests.CreatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyListingDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdatePropertyRequestDTO;
import com.finalproject.stayease.property.repository.PropertyRepository;
import com.finalproject.stayease.property.service.PropertyCategoryService;
import com.finalproject.stayease.users.entity.Users;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PropertyServiceImplTest {

  @Mock
  private PropertyRepository propertyRepository;

  @Mock
  private PropertyCategoryService propertyCategoryService;

  @InjectMocks
  private PropertyServiceImpl propertyService;

  private Users tenant;
  private Property property;
  private CreatePropertyRequestDTO createDTO;
  private UpdatePropertyRequestDTO updateDTO;
  private PropertyCategory category;

  @BeforeEach
  void setUp() {
    tenant = new Users();
    tenant.setId(1L);
    tenant.setUserType(Users.UserType.TENANT);

    category = new PropertyCategory();
    category.setId(1L);

    property = new Property();
    property.setId(1L);
    property.setTenant(tenant);
    property.setCategory(category);

    createDTO = new CreatePropertyRequestDTO();
    createDTO.setCategoryId(1L);
    createDTO.setName("Test Property");
    createDTO.setLongitude(0.0);
    createDTO.setLatitude(0.0);

    updateDTO = new UpdatePropertyRequestDTO();
    updateDTO.setName("Updated Property");
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
    assertThrows(DataNotFoundException.class, () -> propertyService.findAll());
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
    assertThrows(DataNotFoundException.class, () -> propertyService.findAllByTenant(tenant));
  }

  @Test
  void createProperty_Success() {
    when(propertyCategoryService.findCategoryByIdAndNotDeleted(1L)).thenReturn(Optional.of(category));
    when(propertyRepository.findByLocationAndDeletedAtIsNull(any(Point.class))).thenReturn(Optional.empty());
    when(propertyRepository.save(any(Property.class))).thenReturn(property);

    Property result = propertyService.createProperty(tenant, createDTO);
    assertNotNull(result);
    assertEquals(property.getTenant().getId(), result.getTenant().getId());
  }

  @Test
  void createProperty_DuplicateLocation() {
    when(propertyRepository.findByLocationAndDeletedAtIsNull(any(Point.class))).thenReturn(Optional.of(property));

    assertThrows(DuplicateEntryException.class, () -> propertyService.createProperty(tenant, createDTO));
  }

  @Test
  void updateProperty_Success() {
    when(propertyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(property));
    when(propertyRepository.save(any(Property.class))).thenReturn(property);

    Property result = propertyService.updateProperty(tenant, 1L, updateDTO);
    assertNotNull(result);
    assertEquals("Updated Property", result.getName());
  }

  @Test
  void updateProperty_PropertyNotFound() {
    when(propertyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

    assertThrows(InvalidRequestException.class, () -> propertyService.updateProperty(tenant, 1L, updateDTO));
  }

  @Test
  void deleteProperty_Success() {
    when(propertyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(property));
    when(propertyRepository.save(any(Property.class))).thenReturn(property);

    Property result = propertyService.deleteProperty(tenant, 1L);
    assertNotNull(result);
    assertNotNull(result.getDeletedAt());
  }

  @Test
  void deleteProperty_PropertyNotFound() {
    when(propertyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

    assertThrows(InvalidRequestException.class, () -> propertyService.deleteProperty(tenant, 1L));
  }

  @Test
  void hardDeleteStaleProperties_Success() {
    when(propertyRepository.deleteAllDeletedPropertiesOlderThan(any(Instant.class))).thenReturn(1);

    int result = propertyService.hardDeleteStaleProperties(Instant.now());
    assertEquals(1, result);
  }

  @Test
  void findPropertyById_Success() {
    when(propertyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(property));

    Optional<Property> result = propertyService.findPropertyById(1L);
    assertTrue(result.isPresent());
    assertEquals(property.getId(), result.get().getId());
  }

  @Test
  void getAllAvailablePropertiesOnDate_Success() {
    when(propertyRepository.findAvailablePropertiesOnDate(any(LocalDate.class))).thenReturn(
        Collections.singletonList(property));

    List<Property> result = propertyService.getAllAvailablePropertiesOnDate(LocalDate.now().plusDays(1));
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  void getAllAvailablePropertiesOnDate_NoPropertiesFound() {
    when(propertyRepository.findAvailablePropertiesOnDate(any(LocalDate.class))).thenReturn(Collections.emptyList());

    assertThrows(DataNotFoundException.class, () -> propertyService.getAllAvailablePropertiesOnDate(LocalDate.now().plusDays(1)));
  }

  @Test
  void findLowestRoomRate_Success() {
    RoomPriceRateDTO rateDTO = new RoomPriceRateDTO();
    when(propertyRepository.findAvailableRoomRates(1L, LocalDate.now().plusDays(1))).thenReturn(List.of(rateDTO));

    RoomPriceRateDTO result = propertyService.findLowestRoomRate(1L, LocalDate.now().plusDays(1));
    assertNotNull(result);
  }

  @Test
  void findLowestRoomRate_NoRatesFound() {
    when(propertyRepository.findAvailableRoomRates(1L, LocalDate.now().plusDays(1))).thenReturn(Collections.emptyList());

    assertThrows(DataNotFoundException.class, () -> propertyService.findLowestRoomRate(1L, LocalDate.now().plusDays(1)));
  }

  @Test
  void findAvailableProperties_Success() {
    PropertyListingDTO listingDTO = new PropertyListingDTO();
    when(propertyRepository.findAvailableProperties(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(listingDTO));

    List<PropertyListingDTO> result = propertyService.findAvailableProperties(
        LocalDate.now().plusDays(1), LocalDate.now().plusDays(2), "City", 1L, "Search", BigDecimal.ONE, BigDecimal.TEN);
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  void isTenantPropertyOwner_Success() {
    when(propertyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(property));

    boolean result = propertyService.isTenantPropertyOwner(tenant, 1L);
    assertTrue(result);
  }

  @Test
  void isTenantPropertyOwner_NotOwner() {
    Users otherTenant = new Users();
    otherTenant.setId(2L);
    otherTenant.setUserType(Users.UserType.TENANT);
    property.setTenant(otherTenant);

    when(propertyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(property));

    boolean result = propertyService.isTenantPropertyOwner(tenant, 1L);
    assertFalse(result);
  }
}