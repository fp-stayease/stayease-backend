package com.finalproject.stayease.property.service;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.createRequests.CreatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyListingDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdatePropertyRequestDTO;
import com.finalproject.stayease.users.entity.Users;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PropertyService {

  // Property retrieval methods
  List<Property> findAll();
  List<Property> findAllByTenant(Users tenant);
  Optional<Property> findPropertyById(Long id);
  List<Property> findAllPropertiesWithAutoRatesEnabled();
  List<String> findDistinctCities();
  List<String> findAllPropertyRoomImageUrls();

  // Property management methods
  Property createProperty(Users tenant, CreatePropertyRequestDTO requestDTO);
  Property updateProperty(Users tenant, Long propertyId, UpdatePropertyRequestDTO requestDTO);
  Property deleteProperty(Users tenant, Long propertyId);
  int hardDeleteStaleProperties(Instant timestamp);

  // Property availability and pricing methods
  List<Property> getAllAvailablePropertiesOnDate(LocalDate date);
  RoomPriceRateDTO findLowestRoomRate(Long propertyId, LocalDate date);
  List<RoomPriceRateDTO> findAvailableRoomRates(Long propertyId, LocalDate date);

  // Property listing fort sorting and filtering
  List<PropertyListingDTO> findAvailableProperties(
      LocalDate startDate,
      LocalDate endDate,
      String city,
      Long categoryId,
      String searchTerm,
      BigDecimal minPrice,
      BigDecimal maxPrice
  );

  // Property ownership verification
  boolean isTenantPropertyOwner(Users tenant, Long propertyId);
  Long tenantPropertyCount(Users tenant);
}
