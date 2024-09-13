package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.dto.PropertyListingDTO;
import java.time.LocalDate;
import org.springframework.data.domain.Page;

public interface PropertyListingService {
  Page<PropertyListingDTO> findAvailableProperties(
      LocalDate startDate,
      LocalDate endDate,
      String city,
      Long categoryId,
      String searchTerm,
      int page,
      int size,
      String sortBy,
      String sortDirection
  );
}
