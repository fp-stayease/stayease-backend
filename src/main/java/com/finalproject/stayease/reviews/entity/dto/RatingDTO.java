package com.finalproject.stayease.reviews.entity.dto;

import lombok.Data;

@Data
public class RatingDTO {
    private String propertyName;
    private Double avgRating;

    public RatingDTO(String propertyName, Double avgRating) {
        this.propertyName = propertyName;
        this.avgRating = avgRating;
    }
}
