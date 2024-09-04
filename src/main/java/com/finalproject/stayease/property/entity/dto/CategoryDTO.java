package com.finalproject.stayease.property.entity.dto;

import com.finalproject.stayease.property.entity.PropertyCategory;
import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private String addedBy;

    public CategoryDTO(PropertyCategory propertyCategory) {
      this.id = propertyCategory.getId();
      this.name = propertyCategory.getName();
      this.description = propertyCategory.getDescription();
      this.addedBy = propertyCategory.getAddedBy().getTenantInfo().getBusinessName();
    }
}
