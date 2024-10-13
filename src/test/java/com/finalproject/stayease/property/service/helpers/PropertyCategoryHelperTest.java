package com.finalproject.stayease.property.service.helpers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.finalproject.stayease.exceptions.auth.UnauthorizedOperationsException;
import com.finalproject.stayease.exceptions.properties.CategoryNotFoundException;
import com.finalproject.stayease.exceptions.properties.DuplicateCategoryException;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateCategoryRequestDTO;
import com.finalproject.stayease.property.repository.PropertyCategoryRepository;
import com.finalproject.stayease.users.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PropertyCategoryHelperTest {

  @Mock
  private PropertyCategoryRepository propertyCategoryRepository;

  @InjectMocks
  private PropertyCategoryHelper propertyCategoryHelper;

  private Users tenant;
  private PropertyCategory category;
  private CreateCategoryRequestDTO createDTO;

  @BeforeEach
  void setUp() {
    tenant = new Users();
    tenant.setId(1L);
    tenant.setUserType(Users.UserType.TENANT);

    category = new PropertyCategory();
    category.setId(1L);
    category.setName("Apartment");
    category.setAddedBy(tenant);

    createDTO = new CreateCategoryRequestDTO();
    createDTO.setName("New Category");
  }

  @Test
  void isTenant_Success() {
    assertDoesNotThrow(() -> propertyCategoryHelper.isTenant(tenant));
  }

  @Test
  void isTenant_NotTenant() {
    tenant.setUserType(Users.UserType.USER);
    assertThrows(UnauthorizedOperationsException.class, () -> propertyCategoryHelper.isTenant(tenant));
  }

  @Test
  void checkMatch_Success() {
    when(propertyCategoryRepository.findByNameIgnoreCaseAndDeletedAtIsNull(anyString())).thenReturn(Optional.empty());
    when(propertyCategoryRepository.findAll()).thenReturn(java.util.Collections.emptyList());
    assertDoesNotThrow(() -> propertyCategoryHelper.checkMatch("New Category"));
  }

  @Test
  void checkMatch_DuplicateCategory() {
    when(propertyCategoryRepository.findByNameIgnoreCaseAndDeletedAtIsNull(anyString())).thenReturn(Optional.of(category));
    assertThrows(DuplicateCategoryException.class, () -> propertyCategoryHelper.checkMatch("Apartment"));
  }

  @Test
  void checkMatch_SimilarCategory() {
    when(propertyCategoryRepository.findByNameIgnoreCaseAndDeletedAtIsNull(anyString())).thenReturn(Optional.empty());
    when(propertyCategoryRepository.findAll()).thenReturn(java.util.Collections.singletonList(category));
    assertThrows(DuplicateCategoryException.class, () -> propertyCategoryHelper.checkMatch("Apartmant"));
  }

  @Test
  void toPropertyCategoryEntity_Success() {
    PropertyCategory result = propertyCategoryHelper.toPropertyCategoryEntity(tenant, createDTO);
    assertNotNull(result);
    assertEquals("New Category", result.getName());
    assertEquals(tenant, result.getAddedBy());
  }

  @Test
  void checkIfValid_Success() {
    when(propertyCategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));
    PropertyCategory result = propertyCategoryHelper.checkIfValid(tenant, 1L);
    assertNotNull(result);
    assertEquals(category, result);
  }

  @Test
  void checkIfValid_CategoryNotFound() {
    when(propertyCategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());
    assertThrows(CategoryNotFoundException.class, () -> propertyCategoryHelper.checkIfValid(tenant, 1L));
  }

  @Test
  void checkIfValid_UnauthorizedOperation() {
    Users otherTenant = new Users();
    otherTenant.setId(2L);
    otherTenant.setUserType(Users.UserType.TENANT);
    category.setAddedBy(otherTenant);
    when(propertyCategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));
    assertThrows(UnauthorizedOperationsException.class, () -> propertyCategoryHelper.checkIfValid(tenant, 1L));
  }
}