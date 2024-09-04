package com.finalproject.stayease.property.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreatePropertyRequestDTO {

  private Long categoryId;
  private String name;
  private String description;
  private String picture;
  private String address;
  private String city;
  private String country;
  private Double longitude;
  private Double latitude;
}
