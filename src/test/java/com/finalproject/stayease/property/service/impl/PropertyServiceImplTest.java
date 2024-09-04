package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.exceptions.InvalidRequestException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.createRequests.CreatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdatePropertyRequestDTO;
import com.finalproject.stayease.property.repository.PropertyRepository;
import com.finalproject.stayease.property.service.PropertyCategoryService;
import com.finalproject.stayease.users.entity.Users;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;

public class PropertyServiceImplTest {

  @Mock
  private PropertyRepository propertyRepository;

  @Mock
  private PropertyCategoryService propertyCategoryService;

  @InjectMocks
  private PropertyServiceImpl propertyService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreateProperty_ValidRequest() {
    Users tenant = new Users();
    tenant.setUserType(Users.UserType.TENANT);

    CreatePropertyRequestDTO requestDTO = new CreatePropertyRequestDTO();
    requestDTO.setName("Apartment A");
    requestDTO.setDescription("Cozy apartment");
    requestDTO.setPicture("example.jpg");
    requestDTO.setAddress("123 Main St");
    requestDTO.setCity("Anytown");
    requestDTO.setCountry("USA");
    requestDTO.setLongitude(40.730610);
    requestDTO.setLatitude(-73.935242);
    requestDTO.setCategoryId(1L);

    PropertyCategory category = new PropertyCategory();
    category.setId(1L);
    category.setName("Apartment");
    category.setDescription("Residential property for rent");

    Property expectedProperty = new Property();
    expectedProperty.setTenant(tenant);
    expectedProperty.setCategory(category);
    expectedProperty.setName("Apartment A");
    expectedProperty.setDescription("Cozy apartment");
    expectedProperty.setPicture("example.jpg");
    expectedProperty.setAddress("123 Main St");
    expectedProperty.setCity("Anytown");
    expectedProperty.setCountry("USA");
    expectedProperty.setLongitude(40.730610);
    expectedProperty.setLatitude(-73.935242);

    when(propertyCategoryService.findCategoryByIdAndNotDeleted(1L)).thenReturn(Optional.of(category));
    when(propertyRepository.save(any(Property.class))).thenReturn(expectedProperty);

    Property createdProperty = propertyService.createProperty(tenant, requestDTO);

    assertEquals(expectedProperty.getId(), createdProperty.getId());
    verify(propertyCategoryService, times(1)).findCategoryByIdAndNotDeleted(1L);
    verify(propertyRepository, times(1)).save(any(Property.class));
  }

  @Test
  void testCreateProperty_InvalidTenant() {
    Users nonTenant = new Users();
    nonTenant.setUserType(Users.UserType.USER);

    CreatePropertyRequestDTO requestDTO = new CreatePropertyRequestDTO();
    requestDTO.setName("Apartment A");
    requestDTO.setDescription("Cozy apartment");
    requestDTO.setPicture("example.jpg");
    requestDTO.setAddress("123 Main St");
    requestDTO.setCity("Anytown");
    requestDTO.setCountry("USA");
    requestDTO.setLongitude(40.730610);
    requestDTO.setLatitude(-73.935242);
    requestDTO.setCategoryId(1L);

    assertThrows(InvalidRequestException.class, () -> propertyService.createProperty(nonTenant, requestDTO));
  }

  @Test
  void testCreateProperty_CategoryNotFound() {
    Users tenant = new Users();
    tenant.setUserType(Users.UserType.TENANT);

    CreatePropertyRequestDTO requestDTO = new CreatePropertyRequestDTO();
    requestDTO.setName("Apartment A");
    requestDTO.setDescription("Cozy apartment");
    requestDTO.setPicture("example.jpg");
    requestDTO.setAddress("123 Main St");
    requestDTO.setCity("Anytown");
    requestDTO.setCountry("USA");
    requestDTO.setLongitude(40.730610);
    requestDTO.setLatitude(-73.935242);
    requestDTO.setCategoryId(1L);

    when(propertyCategoryService.findCategoryByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

    assertThrows(DataNotFoundException.class, () -> propertyService.createProperty(tenant, requestDTO));
    verify(propertyCategoryService, times(1)).findCategoryByIdAndNotDeleted(1L);
  }

  @Test
  void testCreateProperty_DuplicateLocation() {
    Users tenant = new Users();
    tenant.setUserType(Users.UserType.TENANT);

    CreatePropertyRequestDTO requestDTO = new CreatePropertyRequestDTO();
    requestDTO.setName("Apartment A");
    requestDTO.setDescription("Cozy apartment");
    requestDTO.setPicture("example.jpg");
    requestDTO.setAddress("123 Main St");
    requestDTO.setCity("Anytown");
    requestDTO.setCountry("USA");
    requestDTO.setLongitude(40.730610);
    requestDTO.setLatitude(-73.935242);
    requestDTO.setCategoryId(1L);

    Property existingProperty = new Property();
    existingProperty.setLongitude(40.730610);
    existingProperty.setLatitude(-73.935242);

    when(propertyRepository.findByLocationAndDeletedAtIsNull(any(Point.class))).thenReturn(
        Optional.of(existingProperty));

    assertThrows(DuplicateEntryException.class, () -> propertyService.createProperty(tenant, requestDTO));
    verify(propertyRepository, times(1)).findByLocationAndDeletedAtIsNull(any(Point.class));
  }

  @Test
  void testUpdateProperty_ValidRequest() {
    // Arrange
    Users tenant = new Users();
    tenant.setUserType(Users.UserType.TENANT);

    UpdatePropertyRequestDTO requestDTO = new UpdatePropertyRequestDTO();
    requestDTO.setCategoryId(2L);
    requestDTO.setName("Apartment A");
    requestDTO.setDescription("Cozy apartment");
    requestDTO.setPicture("example.jpg");

    PropertyCategory newCategory = new PropertyCategory();
    newCategory.setId(2L);

    Long propertyId = 1L;

    PropertyCategory existingCategory = new PropertyCategory();
    existingCategory.setId(1L);

    Property existingProperty = new Property();
    existingProperty.setTenant(tenant);
    existingProperty.setId(propertyId);
    existingProperty.setCategory(existingCategory);
    existingProperty.setName("Apartment B");
    existingProperty.setDescription("Home sweet home");
    existingProperty.setPicture("oldExample.jpg");

    // Act
    when(propertyCategoryService.findCategoryByIdAndNotDeleted(2L)).thenReturn(Optional.of(newCategory));
    when(propertyCategoryService.findCategoryByIdAndNotDeleted(1L)).thenReturn(Optional.of(existingCategory));
    when(propertyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existingProperty));
    Property updatedProperty = propertyService.updateProperty(tenant, propertyId, requestDTO);

    // Assert
    assertNotNull(updatedProperty);
    assertEquals(updatedProperty.getCategory(), existingProperty.getCategory());
    assertEquals(updatedProperty.getId(), existingProperty.getId());
  }

  @Test
  void testUpdateProperty_InvalidProperty() {
    // Assert
    Long propertyId = 2L;

    UpdatePropertyRequestDTO requestDTO  = new UpdatePropertyRequestDTO();
    Users tenant = new Users();
    tenant.setUserType(Users.UserType.TENANT);

    Property existingProperty = new Property();
    existingProperty.setId(1L);

    // Act
    when(propertyRepository.findByIdAndDeletedAtIsNull(2L)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(InvalidRequestException.class, () -> propertyService.updateProperty(tenant, propertyId, requestDTO));
  }

  @Test
  void testDeleteProperty_InvalidTenant() {
    // Arrange
    Long propertyId = 1L;
    UpdatePropertyRequestDTO requestDTO = new UpdatePropertyRequestDTO();
    Users requestedTenant = new Users();
    requestedTenant.setId(2L);
    requestedTenant.setUserType(Users.UserType.TENANT);

    Users tenant = new Users();
    tenant.setId(1L);
    requestedTenant.setUserType(Users.UserType.TENANT);
    Property existingProperty = new Property();
    existingProperty.setId(propertyId);
    existingProperty.setTenant(tenant);

    // Act
    when(propertyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existingProperty));

    // Act & Assert
    assertThrows(BadCredentialsException.class, () -> propertyService.updateProperty(requestedTenant, propertyId, requestDTO));
  }
}
