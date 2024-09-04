package com.finalproject.stayease.property.entity.dto.updateRequests;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UpdateCategoryRequestDTO {

  // TODO : determine if name is updateable bc it will affect other properties that used it
//  private String name;

  @NotEmpty
  private String description;
}
