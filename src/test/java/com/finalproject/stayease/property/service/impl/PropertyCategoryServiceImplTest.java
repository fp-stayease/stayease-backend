package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.finalproject.stayease.exceptions.properties.CategoryNotFoundException;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateCategoryRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateCategoryRequestDTO;
import com.finalproject.stayease.property.repository.PropertyCategoryRepository;
import com.finalproject.stayease.property.service.helpers.PropertyCategoryHelper;
import com.finalproject.stayease.users.entity.Users;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PropertyCategoryServiceImplTest {

  @Mock
  private PropertyCategoryRepository propertyCategoryRepository;

  @Mock
  private PropertyCategoryHelper propertyCategoryHelper;

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
    when(propertyCategoryHelper.toPropertyCategoryEntity(tenant, createDTO)).thenReturn(category);
    PropertyCategory result = propertyCategoryService.createCategory(tenant, createDTO);
    assertNotNull(result);
    assertEquals("Apartment", result.getName());
    verify(propertyCategoryHelper).isTenant(tenant);
    verify(propertyCategoryHelper).checkMatch(createDTO.getName());
  }

  @Test
  void updateCategory_Success() {
    when(propertyCategoryHelper.checkIfValid(tenant, 1L)).thenReturn(category);
    when(propertyCategoryRepository.save(any(PropertyCategory.class))).thenReturn(category);

    PropertyCategory result = propertyCategoryService.updateCategory(1L, tenant, updateDTO);
    assertNotNull(result);
    assertEquals("Updated Description", result.getDescription());
  }

  @Test
  void deleteCategory_Success() {
    when(propertyCategoryHelper.checkIfValid(tenant, 1L)).thenReturn(category);
    when(propertyCategoryRepository.save(any(PropertyCategory.class))).thenReturn(category);

    assertDoesNotThrow(() -> propertyCategoryService.deleteCategory(1L, tenant));
    assertNotNull(category.getDeletedAt());
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