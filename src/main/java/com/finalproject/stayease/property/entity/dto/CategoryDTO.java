package com.finalproject.stayease.property.entity.dto;

import com.finalproject.stayease.property.entity.PropertyCategory;
import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;

    public CategoryDTO(PropertyCategory propertyCategory) {
      this.id = propertyCategory.getId();
      this.name = propertyCategory.getName();
    }
}
