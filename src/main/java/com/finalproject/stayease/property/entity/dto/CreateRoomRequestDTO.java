package com.finalproject.stayease.property.entity.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateRoomRequestDTO {
  private Long propertyId;
  private String name;
  private String description;
  private BigDecimal basePrice;
  private Integer capacity;
}
