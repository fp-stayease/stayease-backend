package com.finalproject.stayease.property.entity.dto.createRequests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePropertyRequestDTO {

  private Long categoryId;
  private String name;
  private String description;
  private String imageUrl;
  private String address;
  private String city;
  private String country;
  private Double longitude;
  private Double latitude;
}
