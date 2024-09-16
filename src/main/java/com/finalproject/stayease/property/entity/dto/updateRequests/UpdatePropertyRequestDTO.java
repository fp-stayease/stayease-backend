package com.finalproject.stayease.property.entity.dto.updateRequests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePropertyRequestDTO {
  private Long categoryId;
  private String name;
  private String description;
  private String images;

  // * Shouldn't be able to change address or location because of the complication it may cause
//  private String address;
//  private String city;
//  private String country;
//  private Double longitude;
//  private Double latitude;
}
