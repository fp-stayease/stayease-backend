package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.properties.CategoryNotFoundException;
import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateCategoryRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateCategoryRequestDTO;
import com.finalproject.stayease.property.repository.PropertyCategoryRepository;
import com.finalproject.stayease.property.service.PropertyCategoryService;
import com.finalproject.stayease.property.service.helpers.PropertyCategoryHelper;
import com.finalproject.stayease.users.entity.Users;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Data
@Transactional
@Slf4j
public class PropertyCategoryServiceImpl implements PropertyCategoryService {

  private final PropertyCategoryRepository propertyCategoryRepository;
  private final PropertyCategoryHelper propertyCategoryHelper;

  /**
   * Retrieves all property categories.
   */
  @Override
  public List<PropertyCategory> findAll() {
    List<PropertyCategory> categoryList = propertyCategoryRepository.findAll();
    if (categoryList.isEmpty()) {
      throw new CategoryNotFoundException("No PropertyCategory found");
    }
    return categoryList;
  }

  /**
   * Creates a new property category.
   */
  @Override
  public PropertyCategory createCategory(Users tenant, CreateCategoryRequestDTO requestDTO) {
    propertyCategoryHelper.isTenant(tenant);
    propertyCategoryHelper.checkMatch(requestDTO.getName());
    return propertyCategoryHelper.toPropertyCategoryEntity(tenant, requestDTO);
  }

  /**
   * Updates an existing property category.
   */
  @Override
  public PropertyCategory updateCategory(Long categoryId, Users tenant, UpdateCategoryRequestDTO requestDTO) {
    PropertyCategory existingCategory = propertyCategoryHelper.checkIfValid(tenant, categoryId);
    Optional.ofNullable(requestDTO.getDescription()).ifPresent(existingCategory::setDescription);
    return propertyCategoryRepository.save(existingCategory);
  }

  /**
   * Deletes a property category.
   */
  @Override
  public void deleteCategory(Long categoryId, Users tenant) {
    PropertyCategory existingCategory = propertyCategoryHelper.checkIfValid(tenant, categoryId);
    existingCategory.setDeletedAt(Instant.now());
    propertyCategoryRepository.save(existingCategory);
  }

  /**
   * Finds a non-deleted property category by its ID.
   */
  @Override
  public Optional<PropertyCategory> findCategoryByIdAndNotDeleted(Long id) {
    return propertyCategoryRepository.findByIdAndDeletedAtIsNull(id);
  }
}
