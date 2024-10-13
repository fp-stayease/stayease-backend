package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.createRequests.CreateCategoryRequestDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdateCategoryRequestDTO;
import com.finalproject.stayease.users.entity.Users;
import java.util.List;
import java.util.Optional;

public interface PropertyCategoryService {

  List<PropertyCategory> findAll();

  PropertyCategory createCategory(Users tenant, CreateCategoryRequestDTO requestDTO);
  PropertyCategory updateCategory(Long categoryId, Users tenant, UpdateCategoryRequestDTO requestDTO);
  void deleteCategory(Long categoryId, Users tenant);

  // Region - helpers
  Optional<PropertyCategory> findCategoryByIdAndNotDeleted(Long id);
}
