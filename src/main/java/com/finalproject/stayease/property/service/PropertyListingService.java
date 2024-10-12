package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyAvailableOnDateDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyListingDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;

public interface PropertyListingService {

  Page<PropertyListingDTO> findAvailableProperties(
      LocalDate startDate,
      LocalDate endDate,
      String city,
      String categoryName,
      String searchTerm,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Integer guestCount,
      int page,
      int size,
      String sortBy,
      String sortDirection
  );

  PropertyAvailableOnDateDTO findAvailablePropertyOnDate(Long propertyId, LocalDate date);

  List<PropertyListingDTO> findPropertiesWithLowestRoomRate(LocalDate date);
}
