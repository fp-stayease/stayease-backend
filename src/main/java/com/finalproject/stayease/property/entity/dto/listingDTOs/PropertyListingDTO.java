package com.finalproject.stayease.property.entity.dto.listingDTOs;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyListingDTO {

  private Long propertyId;
  private String propertyName;
  private String description;
  private String imageUrl;
  private String city;
  private String categoryName;
  private Double longitude;
  private Double latitude;
  private BigDecimal lowestBasePrice;
  private BigDecimal lowestAdjustedPrice;

  public PropertyListingDTO(Long propertyId, String propertyName, String description, String imageUrl,
      String city, String categoryName, Double longitude, Double latitude, BigDecimal lowestBasePrice, BigDecimal lowestAdjustedPrice) {
    this.propertyId = propertyId;
    this.propertyName = propertyName;
    this.description = description;
    this.imageUrl = imageUrl;
    this.city = city;
    this.categoryName = categoryName;
    this.longitude = longitude;
    this.latitude = latitude;
    this.lowestBasePrice = lowestBasePrice;
    this.lowestAdjustedPrice = lowestAdjustedPrice; // Will be set in service layer
  }


}
