package com.finalproject.stayease.property.entity.dto.listingDTOs;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.Room;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyAvailableOnDateDTO {
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
  private List<RoomAdjustedRatesDTO> rooms;
  private List<UnavailableRoomDTO> unavailableRooms;

  public PropertyAvailableOnDateDTO(Property property, List<RoomAdjustedRatesDTO> rooms, List<Room> unavailableRooms) {
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
    this.rooms = rooms;
    if (unavailableRooms != null) {
      this.unavailableRooms = unavailableRooms.stream().map(UnavailableRoomDTO::new).collect(Collectors.toList());
    }
  }

  @Data
  public static class UnavailableRoomDTO {
    private Long propertyId;
    private Long roomId;
    private String roomName;
    private String imageUrl;
    private Integer roomCapacity;
    private BigDecimal basePrice;

    public UnavailableRoomDTO(Room room) {
      this.propertyId = room.getProperty().getId();
      this.roomId = room.getId();
      this.roomName = room.getName();
      this.imageUrl = room.getImageUrl();
      this.roomCapacity = room.getCapacity();
      this.basePrice = room.getBasePrice();
    }

  }
}
