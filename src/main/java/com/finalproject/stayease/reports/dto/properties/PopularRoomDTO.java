package com.finalproject.stayease.reports.dto.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PopularRoomDTO {
    private String room;
    private String property;
    private String roomImg;
    private Long totalBooked;
}