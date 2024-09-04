package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.PropertyCategory;
import com.finalproject.stayease.property.entity.dto.CreateCategoryRequestDTO;
import com.finalproject.stayease.users.entity.Users;
import java.util.Optional;

public interface PropertyCategoryService {

  PropertyCategory createCategory(Users tenant, CreateCategoryRequestDTO requestDTO);

  // Region - helpers
  Optional<PropertyCategory> findCategoryById(Long id);
}
