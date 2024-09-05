package com.finalproject.stayease.property.entity.dto.createRequests;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoomRequestDTO {
  private String name;
  private String description;
  private BigDecimal basePrice;
  private Integer capacity;
}
