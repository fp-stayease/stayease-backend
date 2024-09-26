package com.finalproject.stayease.property.service;

import com.fasterxml.jackson.databind.annotation.JsonAppend.Prop;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.PropertyCurrentDTO;
import com.finalproject.stayease.property.entity.dto.createRequests.CreatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyListingDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdatePropertyRequestDTO;
import com.finalproject.stayease.users.entity.Users;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PropertyService {

  List<Property> findAll();

  List<Property> findAllByTenant(Users tenant);

  List<String> findDistinctCities();

  List<String> findAllPropertyRoomImageUrls();

  Property createProperty(Users tenant, CreatePropertyRequestDTO requestDTO);

  Property updateProperty(Users tenant, Long propertyId, UpdatePropertyRequestDTO requestDTO);

  void deleteProperty(Users tenant, Long propertyId);

  Optional<Property> findPropertyById(Long id);

  List<Property> getAllAvailablePropertiesOnDate(LocalDate date);

  RoomPriceRateDTO findLowestRoomRate(Long propertyId, LocalDate date);

  List<RoomPriceRateDTO> findAvailableRoomRates(Long propertyId, LocalDate date);

  List<PropertyListingDTO> findAvailableProperties(
      LocalDate startDate,
      LocalDate endDate,
      String city,
      Long categoryId,
      String searchTerm,
      BigDecimal minPrice,
      BigDecimal maxPrice
  );

  boolean isTenantPropertyOwner(Users tenant, Long propertyId);
  Long tenantPropertyCount(Users tenant);
}
