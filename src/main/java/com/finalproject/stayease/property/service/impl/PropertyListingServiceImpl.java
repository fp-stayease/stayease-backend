package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.exceptions.properties.PeakSeasonRateNotFoundException;
import com.finalproject.stayease.exceptions.properties.PropertyNotFoundException;
import com.finalproject.stayease.exceptions.utils.InvalidDateException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.Room;
import com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyAvailableOnDateDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyListingDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomAdjustedRatesDTO;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.property.service.PropertyListingService;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.property.service.RoomService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Data
public class PropertyListingServiceImpl implements PropertyListingService {

  private final PropertyService propertyService;
  private final PeakSeasonRateService peakSeasonRateService;
  private final RoomService roomService;

  /**
   * Finds available properties based on various criteria and returns a paginated result.
   */
  @Override
  public Page<PropertyListingDTO> findAvailableProperties(
      LocalDate startDate, LocalDate endDate, String city,
      String categoryName, String searchTerm, BigDecimal minPrice, BigDecimal maxPrice, Integer guestCount, int page, int size, String sortBy, String sortDirection
  ) {
    LocalDate checkInDate = startDate != null ? startDate : LocalDate.now();
    LocalDate checkOutDate = endDate != null ? endDate : checkInDate.plusYears(100);
    validateDate(checkInDate, checkOutDate);

    List<PropertyListingDTO> properties = fetchProperties(checkInDate, checkOutDate, city, categoryName, searchTerm,
        minPrice, maxPrice, guestCount);
    log.info("Properties fetched: {}", properties.size());

    applyPeakSeasonRates(properties, checkInDate);
    sortProperties(properties, sortBy, sortDirection);

    return createPage(properties, page, size, sortBy, sortDirection);
  }

  /**
   * Finds an available property on a specific date.
   */
  @Override
  public PropertyAvailableOnDateDTO findAvailablePropertyOnDate(Long propertyId, LocalDate date) {
    validateDate(date);
    Property property = propertyService.findPropertyById(propertyId)
        .orElseThrow(() -> new PropertyNotFoundException("Property not found"));
    List<RoomAdjustedRatesDTO> rooms = peakSeasonRateService.findAvailableRoomRates(propertyId, date);
    List<Room> unavailableRooms = roomService.getUnavailableRoomsByPropertyIdAndDate(propertyId, date);
    return new PropertyAvailableOnDateDTO(property, rooms, unavailableRooms);
  }

  /**
   * Finds properties with the lowest room rate on a specific date.
   */
  @Override
  public List<PropertyListingDTO> findPropertiesWithLowestRoomRate(LocalDate date) {
    validateDate(date);
    List<Property> properties = propertyService.getAllAvailablePropertiesOnDate(date);
    List<PropertyListingDTO> propertyListings = new ArrayList<>();

    for (Property property : properties) {
      RoomAdjustedRatesDTO lowestRoomRate = peakSeasonRateService.findAvailableRoomRates(property.getId(), date).stream().findFirst()
          .orElseThrow(() -> new PeakSeasonRateNotFoundException("No room rates found for this property"));
      propertyListings.add(new PropertyListingDTO(property, lowestRoomRate));
    }
    return propertyListings;
  }


  private List<PropertyListingDTO> fetchProperties(
      LocalDate startDate, LocalDate endDate, String city, String categoryName,
      String searchTerm, BigDecimal minPrice, BigDecimal maxPrice, Integer guestCount
  ) {
    String lowerCaseSearchTerm = searchTerm != null ? searchTerm.toLowerCase() : null;
    String lowerCaseCity = city != null ? city.toLowerCase() : null;
    String lowerCaseCategory = categoryName != null ? categoryName.toLowerCase() : null;
    return propertyService.findAvailableProperties(
        startDate, endDate, lowerCaseCity, lowerCaseCategory, lowerCaseSearchTerm, minPrice, maxPrice, guestCount);
  }

  private void applyPeakSeasonRates(List<PropertyListingDTO> properties, LocalDate checkDate) {
    for (PropertyListingDTO property : properties) {
      BigDecimal adjustedPrice = applyPeakSeasonRate(property.getPropertyId(), checkDate, property.getLowestBasePrice());
      property.setLowestAdjustedPrice(adjustedPrice);
    }
  }

  private void sortProperties(List<PropertyListingDTO> properties, String sortBy, String sortDirection) {
    Comparator<PropertyListingDTO> comparator = getComparator(sortBy);
    if (sortDirection.equalsIgnoreCase("DESC")) {
      comparator = comparator.reversed();
    }
    properties.sort(comparator);
  }

  private Comparator<PropertyListingDTO> getComparator(String sortBy) {
    if (sortBy.equalsIgnoreCase("price")) {
      return Comparator.comparing(PropertyListingDTO::getLowestAdjustedPrice);
    } else {
      return Comparator.comparing(PropertyListingDTO::getPropertyName);
    }
  }

  private Page<PropertyListingDTO> createPage(List<PropertyListingDTO> properties, int page, int size, String sortBy, String sortDirection) {
    int start = page * size;
    int end = Math.min((page + 1) * size, properties.size());
    List<PropertyListingDTO> pageContent = properties.subList(start, end);
    log.info("Creating page: start={}, end={}, pageContentSize={}, totalSize={}",
        start, end, pageContent.size(), properties.size());
    return new PageImpl<>(pageContent, PageRequest.of(page, size, Sort.by(sortDirection, sortBy)), properties.size());
  }

  private BigDecimal applyPeakSeasonRate(Long propertyId, LocalDate date, BigDecimal basePrice) {
    return peakSeasonRateService.applyPeakSeasonRate(propertyId, date, basePrice, Instant.now());
  }

  private void validateDate(LocalDate date) {
    if (date.isBefore(LocalDate.now())) {
      throw new InvalidDateException("Date is out of valid range: " + date);
    }
  }

  private void validateDate(LocalDate startDate, LocalDate endDate) {
    validateDate(startDate);
    if (startDate.isAfter(endDate)) {
      throw new InvalidDateException("Start date cannot be after end date");
    }
  }
}
