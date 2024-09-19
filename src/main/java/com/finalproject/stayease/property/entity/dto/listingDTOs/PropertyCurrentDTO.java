package com.finalproject.stayease.property.entity.dto;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.Room;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PropertyCurrentDTO {

  private Long id;
  private String tenant;
  private String category;
  private String propertyName;
  private String description;
  private String imageUrl;
  private String address;
  private String city;
  private String country;
  private Double latitude;
  private Double longitude;

  private Set<RoomDTO> rooms = new HashSet<>();

  public PropertyCurrentDTO(Property property, List<Room> availableRooms) {
    this.id = property.getId();
    this.tenant = property.getTenant().getTenantInfo().getBusinessName();
    this.category = property.getCategory().getName();
    this.propertyName = property.getName();
    this.description = property.getDescription();
    this.imageUrl = property.getImageUrl();
    this.address = property.getAddress();
    this.city = property.getCity();
    this.country = property.getCountry();
    this.latitude = property.getLatitude();
    this.longitude = property.getLongitude();
    this.rooms = availableRooms.stream().map(RoomDTO::new).collect(Collectors.toSet());
  }

  @Data
  static class RoomSummary {
    private Long roomId;
    private String roomName;
    private BigDecimal roomBasePrice;
    private String roomImageUrl;

    public RoomSummary(Room room) {
      this.roomId = room.getId();
      this.roomName = room.getName();
      this.roomBasePrice = room.getBasePrice();
      this.roomImageUrl = room.getImageUrl();
    }
  }

}
