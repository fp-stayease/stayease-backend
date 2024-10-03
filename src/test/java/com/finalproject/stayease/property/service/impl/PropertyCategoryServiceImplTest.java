package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.finalproject.stayease.exceptions.auth.UnauthorizedOperationsException;
import com.finalproject.stayease.exceptions.properties.CategoryNotFoundException;
import com.finalproject.stayease.exceptions.properties.DuplicateCategoryException;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateCategoryRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateCategoryRequestDTO;
import com.finalproject.stayease.property.repository.PropertyCategoryRepository;
import com.finalproject.stayease.users.entity.Users;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class PropertyCategoryServiceImplTest {

  @Mock
  private PropertyCategoryRepository propertyCategoryRepository;

  @InjectMocks
  private PropertyCategoryServiceImpl propertyCategoryService;

  private Users tenant;
  private PropertyCategory category;
  private CreateCategoryRequestDTO createDTO;
  private UpdateCategoryRequestDTO updateDTO;

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

    updateDTO = new UpdateCategoryRequestDTO();
    updateDTO.setDescription("Updated Description");
  }

  @Test
  void findAll_Success() {
    when(propertyCategoryRepository.findAll()).thenReturn(Collections.singletonList(category));
    List<PropertyCategory> result = propertyCategoryService.findAll();
    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
  }

  @Test
  void findAll_NoCategories() {
    when(propertyCategoryRepository.findAll()).thenReturn(Collections.emptyList());
    assertThrows(CategoryNotFoundException.class, () -> propertyCategoryService.findAll());
  }

  @Test
  void createCategory_Success() {
    when(propertyCategoryRepository.findByNameIgnoreCaseAndDeletedAtIsNull(anyString())).thenReturn(Optional.empty());
    when(propertyCategoryRepository.findAll()).thenReturn(Collections.emptyList());
    when(propertyCategoryRepository.save(any(PropertyCategory.class))).thenReturn(category);

    PropertyCategory result = propertyCategoryService.createCategory(tenant, createDTO);
    assertNotNull(result);
    assertEquals("New Category", result.getName());
  }

  @Test
  void createCategory_DuplicateName() {
    when(propertyCategoryRepository.findByNameIgnoreCaseAndDeletedAtIsNull(anyString())).thenReturn(Optional.of(category));

    assertThrows(DuplicateCategoryException.class, () -> propertyCategoryService.createCategory(tenant, createDTO));
  }

  @Test
  void createCategory_SimilarName() {
    when(propertyCategoryRepository.findByNameIgnoreCaseAndDeletedAtIsNull(anyString())).thenReturn(Optional.empty());
    when(propertyCategoryRepository.findAll()).thenReturn(Collections.singletonList(category));

    createDTO.setName("Apartmant");  // Similar to "Apartment"
    assertThrows(DuplicateCategoryException.class, () -> propertyCategoryService.createCategory(tenant, createDTO));
  }

  @Test
  void createCategory_NonTenant() {
    tenant.setUserType(Users.UserType.USER);
    assertThrows(UnauthorizedOperationsException.class, () -> propertyCategoryService.createCategory(tenant, createDTO));
  }

  @Test
  void updateCategory_Success() {
    when(propertyCategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));
    when(propertyCategoryRepository.save(any(PropertyCategory.class))).thenReturn(category);

    PropertyCategory result = propertyCategoryService.updateCategory(1L, tenant, updateDTO);
    assertNotNull(result);
    assertEquals("Updated Description", result.getDescription());
  }

  @Test
  void updateCategory_CategoryNotFound() {
    when(propertyCategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

    assertThrows(CategoryNotFoundException.class, () -> propertyCategoryService.updateCategory(1L, tenant, updateDTO));
  }

  @Test
  void updateCategory_NotOwner() {
    Users otherTenant = new Users();
    otherTenant.setId(2L);
    otherTenant.setUserType(Users.UserType.TENANT);
    category.setAddedBy(otherTenant);

    when(propertyCategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));

    assertThrows(BadCredentialsException.class, () -> propertyCategoryService.updateCategory(1L, tenant, updateDTO));
  }

  @Test
  void deleteCategory_Success() {
    when(propertyCategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));
    when(propertyCategoryRepository.save(any(PropertyCategory.class))).thenReturn(category);

    assertDoesNotThrow(() -> propertyCategoryService.deleteCategory(1L, tenant));
    assertNotNull(category.getDeletedAt());
  }

  @Test
  void deleteCategory_CategoryNotFound() {
    when(propertyCategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

    assertThrows(CategoryNotFoundException.class, () -> propertyCategoryService.deleteCategory(1L, tenant));
  }

  @Test
  void deleteCategory_NotOwner() {
    Users otherTenant = new Users();
    otherTenant.setId(2L);
    otherTenant.setUserType(Users.UserType.TENANT);
    category.setAddedBy(otherTenant);

    when(propertyCategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));

    assertThrows(BadCredentialsException.class, () -> propertyCategoryService.deleteCategory(1L, tenant));
  }

  @Test
  void findCategoryByIdAndNotDeleted_Success() {
    when(propertyCategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(category));

    Optional<PropertyCategory> result = propertyCategoryService.findCategoryByIdAndNotDeleted(1L);
    assertTrue(result.isPresent());
    assertEquals(category.getId(), result.get().getId());
  }

  @Test
  void findCategoryByIdAndNotDeleted_NotFound() {
    when(propertyCategoryRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

    Optional<PropertyCategory> result = propertyCategoryService.findCategoryByIdAndNotDeleted(1L);
    assertTrue(result.isEmpty());
  }
}