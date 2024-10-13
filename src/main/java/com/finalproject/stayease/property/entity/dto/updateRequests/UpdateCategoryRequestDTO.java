package com.finalproject.stayease.property.entity.dto.updateRequests;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCategoryRequestDTO {

  @NotNull
  private String description;
}
