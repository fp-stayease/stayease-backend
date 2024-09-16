package com.finalproject.stayease.property.entity.dto.createRequests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePropertyRequestDTO {

  private Long categoryId;
  private String name;
  private String description;
  private String images;
  private String address;
  private String city;
  private String country;
  private Double longitude;
  private Double latitude;
}
