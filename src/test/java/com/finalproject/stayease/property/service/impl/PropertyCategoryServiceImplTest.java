package com.finalproject.stayease.property.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.finalproject.stayease.exceptions.DuplicateEntryException;
import com.finalproject.stayease.exceptions.InvalidRequestException;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateCategoryRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateCategoryRequestDTO;
import com.finalproject.stayease.property.repository.PropertyCategoryRepository;
import com.finalproject.stayease.users.entity.Users;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;

@SpringBootTest
public class PropertyCategoryServiceImplTest {

  @MockBean
  private PropertyCategoryRepository propertyCategoryRepository;

  @MockBean
  private LevenshteinDistance levenshteinDistance;

  @MockBean
  private JaroWinklerSimilarity jaroWinklerSimilarity;

  @InjectMocks
  private PropertyCategoryServiceImpl propertyCategoryService =
      new PropertyCategoryServiceImpl(propertyCategoryRepository);

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    propertyCategoryService = new PropertyCategoryServiceImpl(propertyCategoryRepository);
  }

  @Test
  void testCreateCategory_ValidRequest() {
    Users tenant = new Users();
    tenant.setUserType(Users.UserType.TENANT);

    CreateCategoryRequestDTO requestDTO = new CreateCategoryRequestDTO();
    requestDTO.setName("Apartment");

    PropertyCategory expectedCategory = new PropertyCategory();
    expectedCategory.setName("apartment");
    expectedCategory.setDescription("Residential property for rent");
    expectedCategory.setAddedBy(tenant);

    when(propertyCategoryRepository.findByNameIgnoreCaseAndDeletedAtIsNull("Apartment")).thenReturn(Optional.empty());
    when(propertyCategoryRepository.save(any(PropertyCategory.class))).thenReturn(expectedCategory);

    PropertyCategory createdCategory = propertyCategoryService.createCategory(tenant, requestDTO);

    assertEquals(expectedCategory.getName(), createdCategory.getName());
    assertEquals(expectedCategory.getDescription(), createdCategory.getDescription());
    assertEquals(expectedCategory.getAddedBy(), createdCategory.getAddedBy());
    verify(propertyCategoryRepository, times(1)).findByNameIgnoreCaseAndDeletedAtIsNull("Apartment");
    verify(propertyCategoryRepository, times(1)).save(any(PropertyCategory.class));
  }

  @Test
  void testCreateCategory_DuplicateCategory() {
    Users tenant = new Users();
    tenant.setUserType(Users.UserType.TENANT);

    CreateCategoryRequestDTO requestDTO = new CreateCategoryRequestDTO();
    requestDTO.setName("Apartment");

    PropertyCategory existingCategory = new PropertyCategory();
    existingCategory.setName("apartment");
    existingCategory.setDescription("Residential property for rent");
    existingCategory.setAddedBy(tenant);

    when(propertyCategoryRepository.findByNameIgnoreCaseAndDeletedAtIsNull("Apartment")).thenReturn(Optional.of(existingCategory));

    assertThrows(DuplicateEntryException.class, () -> propertyCategoryService.createCategory(tenant, requestDTO));
    verify(propertyCategoryRepository, times(1)).findByNameIgnoreCaseAndDeletedAtIsNull("Apartment");
  }

  @Test
  void testCreateCategory_InvalidTenant() {
    Users nonTenant = new Users();
    nonTenant.setUserType(Users.UserType.USER);

    CreateCategoryRequestDTO requestDTO = new CreateCategoryRequestDTO();
    requestDTO.setName("Apartment");

    assertThrows(InvalidRequestException.class, () -> propertyCategoryService.createCategory(nonTenant, requestDTO));
  }

  @Test
  void testCreateCategory_SimilarNames() {
    Users tenant = new Users();
    tenant.setUserType(Users.UserType.TENANT);

    CreateCategoryRequestDTO requestDTO = new CreateCategoryRequestDTO();
    requestDTO.setName("Name2");

    PropertyCategory existingCategory = new PropertyCategory();
    existingCategory.setName("Name1");

    when(propertyCategoryRepository.findByNameIgnoreCaseAndDeletedAtIsNull("Name2")).thenReturn(Optional.empty());
    when(propertyCategoryRepository.findAll()).thenReturn(List.of(existingCategory));
    when(levenshteinDistance.apply(anyString(), anyString())).thenReturn(2);
    when(jaroWinklerSimilarity.apply(anyString(), anyString())).thenReturn(0.95);

    assertThrows(RuntimeException.class, () -> propertyCategoryService.createCategory(tenant, requestDTO));
    verify(propertyCategoryRepository, times(1)).findByNameIgnoreCaseAndDeletedAtIsNull("Name2");
    verify(propertyCategoryRepository, times(1)).findAll();
  }

  @Test
  void testUpdateCategory_ValidRequest() {
    Users tenant = new Users();
    tenant.setUserType(Users.UserType.TENANT);

    PropertyCategory existingCategory = new PropertyCategory();
    existingCategory.setId(1L);
    existingCategory.setName("Apartment");
    existingCategory.setDescription("Residential property for rent");
    existingCategory.setAddedBy(tenant);

    UpdateCategoryRequestDTO requestDTO = new UpdateCategoryRequestDTO();
    requestDTO.setDescription("Updated description");
    Long requestedCategoryId = 1L;

    PropertyCategory updatedCategory = new PropertyCategory();
    updatedCategory.setId(1L);
    updatedCategory.setName("Apartment");
    updatedCategory.setDescription("Updated description");
    updatedCategory.setAddedBy(tenant);

    when(propertyCategoryRepository.findByIdAndDeletedAtIsNull(requestedCategoryId)).thenReturn(Optional.of(existingCategory));
    when(propertyCategoryRepository.save(existingCategory)).thenReturn(updatedCategory);

    PropertyCategory result = propertyCategoryService.updateCategory(requestedCategoryId, tenant, requestDTO);

    assertEquals(updatedCategory.getName(), result.getName());
    assertEquals(updatedCategory.getDescription(), result.getDescription());
    assertEquals(updatedCategory.getAddedBy(), result.getAddedBy());
    verify(propertyCategoryRepository, times(1)).findByIdAndDeletedAtIsNull(requestedCategoryId);
    verify(propertyCategoryRepository, times(1)).save(existingCategory);
  }

  @Test
  void testUpdateCategory_CategoryNotFound() {
    Users tenant = new Users();
    tenant.setUserType(Users.UserType.TENANT);

    UpdateCategoryRequestDTO requestDTO = new UpdateCategoryRequestDTO();
    Long requestedCategoryId = 1L;
    requestDTO.setDescription("Updated description");

    when(propertyCategoryRepository.findByIdAndDeletedAtIsNull(requestedCategoryId)).thenReturn(Optional.empty());

    assertThrows(InvalidRequestException.class, () -> propertyCategoryService.updateCategory(requestedCategoryId, tenant, requestDTO));
    verify(propertyCategoryRepository, times(1)).findByIdAndDeletedAtIsNull(requestedCategoryId);
  }

  @Test
  void testUpdateCategory_NotOwner() {
    Users tenant = new Users();
    tenant.setUserType(Users.UserType.TENANT);

    Users otherUser = new Users();
    otherUser.setUserType(Users.UserType.TENANT);

    PropertyCategory existingCategory = new PropertyCategory();
    existingCategory.setId(1L);
    existingCategory.setName("Apartment");
    existingCategory.setDescription("Residential property for rent");
    existingCategory.setAddedBy(otherUser);

    UpdateCategoryRequestDTO requestDTO = new UpdateCategoryRequestDTO();
    Long requestedCategoryId = 1L;
    requestDTO.setDescription("Updated description");

    when(propertyCategoryRepository.findByIdAndDeletedAtIsNull(requestedCategoryId)).thenReturn(Optional.of(existingCategory));

    assertThrows(BadCredentialsException.class, () -> propertyCategoryService.updateCategory(requestedCategoryId, tenant, requestDTO));
    verify(propertyCategoryRepository, times(1)).findByIdAndDeletedAtIsNull(1L);
  }
  
  @Test
  void testDeleteCategory_ValidRequest() {
    Users tenant = new Users();
    tenant.setUserType(Users.UserType.TENANT);

    Instant mockTime = Instant.now();
    
    PropertyCategory existingCategory = new PropertyCategory();
    Long categoryId = 1L;
    existingCategory.setAddedBy(tenant);
    existingCategory.setId(categoryId);
    existingCategory.setDeletedAt(mockTime);
    
    when(propertyCategoryRepository.findByIdAndDeletedAtIsNull(categoryId)).thenReturn(Optional.of(existingCategory));
    
    propertyCategoryService.deleteCategory(categoryId, tenant);

    verify(propertyCategoryRepository, times(1)).findByIdAndDeletedAtIsNull(categoryId);
    assertNotNull(existingCategory.getDeletedAt());
  }
}