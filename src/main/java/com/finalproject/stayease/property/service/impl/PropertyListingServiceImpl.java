package com.finalproject.stayease.property.service.impl;

import com.finalproject.stayease.property.entity.dto.PropertyListingDTO;
import com.finalproject.stayease.property.service.PeakSeasonRateService;
import com.finalproject.stayease.property.service.PropertyListingService;
import com.finalproject.stayease.property.service.PropertyService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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

  @Override
  public Page<PropertyListingDTO> findAvailableProperties(
      LocalDate startDate, LocalDate endDate, String city, Long categoryId,
      String searchTerm, int page, int size, String sortBy, String sortDirection
  ) {
    LocalDate checkDate = startDate != null ? startDate : LocalDate.now();
    List<PropertyListingDTO> properties = fetchProperties(startDate, endDate, city, categoryId, searchTerm);
    applyPeakSeasonRates(properties, checkDate);
    sortProperties(properties, sortBy, sortDirection);
    return createPage(properties, page, size, sortBy, sortDirection);
  }

  private List<PropertyListingDTO> fetchProperties(
      LocalDate startDate, LocalDate endDate, String city, Long categoryId,
      String searchTerm
  ) {
    String lowerCaseSearchTerm = searchTerm != null ? searchTerm.toLowerCase() : null;
    return propertyService.findAvailableProperties(
        startDate, endDate, city, categoryId, lowerCaseSearchTerm);
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
    return new PageImpl<>(pageContent, PageRequest.of(page, size, Sort.by(sortDirection, sortBy)), properties.size());
  }

  private BigDecimal applyPeakSeasonRate(Long propertyId, LocalDate date, BigDecimal basePrice) {
    return peakSeasonRateService.applyPeakSeasonRate(propertyId, date, basePrice, Instant.now());
  }
}
