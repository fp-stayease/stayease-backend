package com.finalproject.stayease.property.entity.dto.listingDTOs;

import com.finalproject.stayease.property.entity.Property;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PropertyListingDTO {

  private Long propertyId;
  private String businessName;
  private String propertyName;
  private String description;
  private String imageUrl;
  private String address;
  private String city;
  private String country;
  private String categoryName;
  private Double longitude;
  private Double latitude;
  private BigDecimal lowestBasePrice;
  private BigDecimal lowestAdjustedPrice;

  public PropertyListingDTO(Property property, RoomAdjustedRatesDTO adjustedRate) {
    this.propertyId = property.getId();
    this.businessName = property.getTenant().getTenantInfo().getBusinessName();
    this.propertyName = property.getName();
    this.description = property.getDescription();
    this.imageUrl = property.getImageUrl();
    this.address = property.getAddress();
    this.city = property.getCity();
    this.country = property.getCountry();
    this.categoryName = property.getCategory().getName();
    this.longitude = property.getLongitude();
    this.latitude = property.getLatitude();
    this.lowestBasePrice = adjustedRate.getBasePrice();
    this.lowestAdjustedPrice = adjustedRate.getAdjustedPrice();
  }


}
