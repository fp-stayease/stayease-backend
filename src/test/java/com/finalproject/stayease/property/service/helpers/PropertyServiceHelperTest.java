package com.finalproject.stayease.property.service.helpers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.finalproject.stayease.exceptions.auth.UnauthorizedOperationsException;
import com.finalproject.stayease.exceptions.properties.CategoryNotFoundException;
import com.finalproject.stayease.exceptions.properties.DuplicatePropertyException;
import com.finalproject.stayease.exceptions.properties.PropertyNotFoundException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.createRequests.CreatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdatePropertyRequestDTO;
import com.finalproject.stayease.property.repository.PropertyRepository;
import com.finalproject.stayease.property.service.PropertyCategoryService;
import com.finalproject.stayease.users.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class PropertyServiceHelperTest {

  @Mock
  private PropertyRepository propertyRepository;

  @Mock
  private PropertyCategoryService propertyCategoryService;

  @InjectMocks
  private PropertyServiceHelper propertyServiceHelper;

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
  void isTenant_Success() {
    assertDoesNotThrow(() -> propertyServiceHelper.isTenant(tenant));
  }

  @Test
  void isTenant_NotTenant() {
    tenant.setUserType(Users.UserType.USER);
    assertThrows(UnauthorizedOperationsException.class, () -> propertyServiceHelper.isTenant(tenant));
  }

  @Test
  void toPropertyEntity_Success() {
    when(propertyCategoryService.findCategoryByIdAndNotDeleted(1L)).thenReturn(Optional.of(category));
    when(propertyRepository.findByLocationAndDeletedAtIsNull(any(Point.class))).thenReturn(Optional.empty());
    when(propertyRepository.save(any(Property.class))).thenReturn(property);

    Property result = propertyServiceHelper.toPropertyEntity(tenant, createDTO);
    assertNotNull(result);
    assertEquals(property.getTenant(), result.getTenant());
  }

  @Test
  void toPropertyEntity_DuplicateLocation() {
    when(propertyRepository.findByLocationAndDeletedAtIsNull(any(Point.class))).thenReturn(Optional.of(property));

    assertThrows(DuplicatePropertyException.class, () -> propertyServiceHelper.toPropertyEntity(tenant, createDTO));
  }

  @Test
  void checkIfValid_Success() {
    when(propertyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(property));
    Property result = propertyServiceHelper.checkIfValid(tenant, 1L);
    assertNotNull(result);
    assertEquals(property, result);
  }

  @Test
  void checkIfValid_PropertyNotFound() {
    when(propertyRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
    assertThrows(PropertyNotFoundException.class, () -> propertyServiceHelper.checkIfValid(tenant, 1L));
  }

  @Test
  void update_Success() {
    when(propertyRepository.save(any(Property.class))).thenReturn(property);
    Property result = propertyServiceHelper.update(property, updateDTO);
    assertNotNull(result);
    assertEquals("Updated Property", result.getName());
  }

  @Test
  void validateDate_Success() {
    LocalDate futureDate = LocalDate.now().plusDays(1);
    assertDoesNotThrow(() -> propertyServiceHelper.validateDate(futureDate));
  }

  @Test
  void validateDate_PastDate() {
    LocalDate pastDate = LocalDate.now().minusDays(1);
    assertThrows(IllegalArgumentException.class, () -> propertyServiceHelper.validateDate(pastDate));
  }

  @Test
  void getCategoryById_Success() {
    when(propertyCategoryService.findCategoryByIdAndNotDeleted(1L)).thenReturn(Optional.of(category));
    PropertyCategory result = propertyServiceHelper.getCategoryById(1L);
    assertNotNull(result);
    assertEquals(category, result);
  }

  @Test
  void getCategoryById_CategoryNotFound() {
    when(propertyCategoryService.findCategoryByIdAndNotDeleted(1L)).thenReturn(Optional.empty());
    assertThrows(CategoryNotFoundException.class, () -> propertyServiceHelper.getCategoryById(1L));
  }
}