package com.finalproject.stayease.property.entity.dto;

import com.finalproject.stayease.property.entity.Room;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class RoomWithRoomAvailabilityDTO {
    private Long id;
    private String name;
    private PropertySummary propertySummary;
    private Set<RoomAvailabilityDTO> roomAvailability;

    public RoomWithRoomAvailabilityDTO(Room room) {
        this.id = room.getId();
        this.name = room.getName();
        this.propertySummary = new PropertySummary(room.getProperty().getId(), room.getProperty().getName(), room.getProperty().getImageUrl());
        this.roomAvailability = room.getRoomAvailabilities().stream().map(RoomAvailabilityDTO::new).collect(Collectors.toSet());
    }

    @Data
    @AllArgsConstructor
    private static class PropertySummary {
        private Long propertyId;
        private String propertyName;
        private String imageUrl;
    }
}
