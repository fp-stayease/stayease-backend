package com.finalproject.stayease.property.entity.dto;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.Room;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;

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

  private Set<RoomSummary> rooms = new HashSet<>();

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
    this.rooms = property.getRooms().stream().map(RoomSummary::new).collect(Collectors.toSet());
  }

  @Data
  static class RoomSummary {
    private Long roomId;
    private String roomName;

    public RoomSummary(Room room) {
      this.roomId = room.getId();
      this.roomName = room.getName();
    }
  }

}
