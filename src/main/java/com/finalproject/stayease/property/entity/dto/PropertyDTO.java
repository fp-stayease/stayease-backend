package com.finalproject.stayease.property.entity.dto;

import com.finalproject.stayease.property.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.locationtech.jts.geom.Point;

@Data
@AllArgsConstructor
public class PropertyDTO {

  private Long id;
  private String tenant; // TODO : make tenants business name not null
  private String category;
  private String propertyName;
  private String description;
  private String picture;
  private String address;
  private String city;
  private String country;
  private Point location;
//  private Set<Room> rooms = new HashSet<>(); TODO : add this later after making room DTO

  public PropertyDTO(Property property) {
    this.id = property.getId();
    this.tenant = property.getTenant().getTenantInfo().getBusinessName();
    this.category = property.getCategory().getName();
    this.propertyName = property.getName();
    this.description = property.getDescription();
    this.picture = property.getPicture();
    this.address = property.getAddress();
    this.city = property.getCity();
    this.country = property.getCountry();
    this.location = property.getLocation();
//    this.rooms = property.getRooms();
  }

  public PropertyDTO toPropertyDTO(Property property) {
    return new PropertyDTO(property);
  }

}
