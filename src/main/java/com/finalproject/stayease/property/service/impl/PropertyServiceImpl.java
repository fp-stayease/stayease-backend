package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.properties.PeakSeasonRateNotFoundException;
import com.finalproject.stayease.exceptions.properties.PropertyNotFoundException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.createRequests.CreatePropertyRequestDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyListingDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import com.finalproject.stayease.property.entity.dto.updateRequests.UpdatePropertyRequestDTO;
import com.finalproject.stayease.property.repository.PropertyRepository;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.helpers.PropertyServiceHelper;
import com.finalproject.stayease.users.entity.Users;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Data
@Slf4j
public class PropertyServiceImpl implements PropertyService {

  private final PropertyRepository propertyRepository;
  private final PropertyServiceHelper propertyServiceHelper;


  // Property retrieval methods

  /**
   * Retrieves all properties from the database.
   * @return A list of all properties.
   * @throws PropertyNotFoundException if no properties are found.
   */
  @Override
  public List<Property> findAll() {
    List<Property> propertyList = propertyRepository.findAll();
    if (propertyList.isEmpty()) {
      throw new PropertyNotFoundException("No property found");
    }
    return propertyList;
  }

  /**
   * Retrieves all properties owned by a specific tenant.
   * @param tenant The tenant user.
   * @return A list of properties owned by the tenant.
   * @throws PropertyNotFoundException if the tenant has no properties.
   */
  @Override
  public List<Property> findAllByTenant(Users tenant) {
    propertyServiceHelper.isTenant(tenant);
    List<Property> tenantsProperties = propertyRepository.findByTenantAndDeletedAtIsNull(tenant);
    if (tenantsProperties.isEmpty()) {
      throw new PropertyNotFoundException("You have no properties yet");
    }
    return tenantsProperties;
  }

  /**
   * Finds a property by its ID.
   * @param id The ID of the property to find.
   * @return An Optional containing the property if found, or empty if not found.
   */
  @Override
  public Optional<Property> findPropertyById(Long id) {
    return propertyRepository.findByIdAndDeletedAtIsNull(id);
  }

  /**
   * Retrieves all properties with auto rates enabled.
   * @return A list of properties with auto rates enabled.
   */
  @Override
  public List<Property> findAllPropertiesWithAutoRatesEnabled() {
    return propertyRepository.findAllPropertiesWithAutoRatesEnabled();
  }

  /**
   * Finds all distinct cities where properties are located.
   * @return A list of distinct city names.
   */
  @Override
  public List<String> findDistinctCities() {
    return propertyRepository.findDistinctCities();
  }

  /**
   * Retrieves all image URLs for properties and their rooms.
   * @return A list of image URLs.
   */
  @Override
  public List<String> findAllPropertyRoomImageUrls() {
    return propertyRepository.findAllPropertyRoomImageUrls();
  }

  // Property management methods

  /**
   * Creates a new property for a tenant.
   * @param tenant The tenant creating the property.
   * @param requestDTO The property details.
   * @return The created property.
   */
  @Override
  public Property createProperty(Users tenant, CreatePropertyRequestDTO requestDTO) {
    propertyServiceHelper.isTenant(tenant);
    return propertyServiceHelper.toPropertyEntity(tenant, requestDTO);
  }

  /**
   * Updates an existing property.
   * @param tenant The tenant owning the property.
   * @param propertyId The ID of the property to update.
   * @param requestDTO The updated property details.
   * @return The updated property.
   */
  @Override
  public Property updateProperty(Users tenant, Long propertyId, UpdatePropertyRequestDTO requestDTO) {
    Property existingProperty = propertyServiceHelper.checkIfValid(tenant, propertyId);
    return propertyServiceHelper.update(existingProperty, requestDTO);
  }

  /**
   * Soft deletes a property by setting its deletedAt timestamp.
   * @param tenant The tenant owning the property.
   * @param propertyId The ID of the property to delete.
   * @return The deleted property.
   */
  @Override
  public Property deleteProperty(Users tenant, Long propertyId) {
    Property existingProperty = propertyServiceHelper.checkIfValid(tenant, propertyId);
    existingProperty.setDeletedAt(Instant.now());
    propertyRepository.save(existingProperty);
    return existingProperty;
  }

  /**
   * Permanently deletes properties that were soft deleted before a given timestamp.
   * @param timestamp The cutoff timestamp for deletion.
   * @return The number of properties deleted.
   */
  @Override
  public int hardDeleteStaleProperties(Instant timestamp) {
    return propertyRepository.deleteAllDeletedPropertiesOlderThan(timestamp);
  }

  // Property availability and pricing methods

  /**
   * Retrieves all properties available on a specific date.
   * @param date The date to check availability.
   * @return A list of available properties.
   */
  @Override
  public List<Property> getAllAvailablePropertiesOnDate(LocalDate date) {
    propertyServiceHelper.validateDate(date);
    List<Property> availableProperties = propertyRepository.findAvailablePropertiesOnDate(date);
    if (availableProperties.isEmpty()) {
      throw new PropertyNotFoundException("No properties found for this date");
    }
    return availableProperties;
  }

  /**
   * Finds the lowest room rate for a property on a specific date.
   * @param propertyId The ID of the property.
   * @param date The date to check rates.
   * @return The lowest room rate.
   */
  @Override
  public RoomPriceRateDTO findLowestRoomRate(Long propertyId, LocalDate date) {
    propertyServiceHelper.validateDate(date);
    return propertyRepository.findAvailableRoomRates(propertyId, date).stream().findFirst().orElseThrow(
        () -> new PeakSeasonRateNotFoundException("No room rates found for this property")
    );
  }

  /**
   * Finds all available room rates for a property on a specific date.
   * @param propertyId The ID of the property.
   * @param date The date to check rates.
   * @return A list of available room rates.
   */
  @Override
  public List<RoomPriceRateDTO> findAvailableRoomRates(Long propertyId, LocalDate date) {
    propertyServiceHelper.validateDate(date);
    propertyRepository.findByIdAndDeletedAtIsNull(propertyId).orElseThrow(
        () -> new PropertyNotFoundException("Property with this ID does not exist or is deleted")
    );
    return propertyRepository.findAvailableRoomRates(propertyId, date);
  }

  /**
   * Finds available properties based on various criteria.
   *
   * @param startDate    The start date of the stay.
   * @param endDate      The end date of the stay.
   * @param city         The city to search in.
   * @param categoryName The category of the property.
   * @param searchTerm   A search term to filter properties.
   * @param minPrice     The minimum price.
   * @param maxPrice     The maximum price.
   * @param guestCount   The minimum number of guests that could stay in the room
   * @return A list of property listings matching the criteria.
   */
  @Override
  public List<PropertyListingDTO> findAvailableProperties(
      LocalDate startDate,
      LocalDate endDate,
      String city,
      String categoryName, String searchTerm,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Integer guestCount) {
    propertyServiceHelper.validateDate(startDate, endDate);
    return propertyRepository.findAvailableProperties(startDate, endDate, city, categoryName, searchTerm, minPrice,
        maxPrice, guestCount);
  }

  // Property ownership verification

  /**
   * Checks if a tenant is the owner of a specific property.
   * @param tenant The tenant to check.
   * @param propertyId The ID of the property.
   * @return true if the tenant owns the property, false otherwise.
   */
  @Override
  public boolean isTenantPropertyOwner(Users tenant, Long propertyId) {
    try {
      propertyServiceHelper.checkIfValid(tenant, propertyId);
    } catch (RuntimeException e) {
      return false;
    }
    return true;
  }

  @Override
  public Long tenantPropertyCount(Users tenant) {
    propertyServiceHelper.isTenant(tenant);
    return propertyRepository.countPropertiesByTenantId(tenant.getId());
  }
}
