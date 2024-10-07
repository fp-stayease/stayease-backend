package com.finalproject.stayease.reports.dto.user;

import lombok.Data;

@Data
public class UserStatsDTO {
    private Double upcomingTrips;
    private Double pastStays;

    public UserStatsDTO(Double upcomingTrips, Double pastStays) {
        this.upcomingTrips = upcomingTrips;
        this.pastStays = pastStays;
    }
}
