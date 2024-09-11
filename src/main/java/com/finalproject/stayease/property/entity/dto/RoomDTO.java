package com.finalproject.stayease.property.entity.dto;

import com.finalproject.stayease.property.entity.Room;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class RoomDTO {
 private Long id;
 private String name;
 private String description;
 private BigDecimal basePrice;
 private Integer capacity;
 private PropertySummary propertySummary;

 public RoomDTO(Room room) {
   this.id = room.getId();
   this.name = room.getName();
   this.description = room.getDescription() != null ? room.getDescription() :  null;
   this.basePrice = room.getBasePrice();
   this.capacity = room.getCapacity();
   this.propertySummary = new PropertySummary(room.getProperty().getId(), room.getProperty().getName(),
       room.getImageUrl());
 }

 @Data
 @AllArgsConstructor
 private static class PropertySummary {
   private Long propertyId;
   private String propertyName;
   private String imageUrl;
 }
}
