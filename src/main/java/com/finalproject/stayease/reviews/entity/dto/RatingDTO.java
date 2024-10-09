package com.finalproject.stayease.reviews.entity.dto;

import lombok.Data;

@Data
public class RatingDTO {
    private String propertyName;
    private Double avgRating;
    private Long totalReviewers;

    public RatingDTO(String propertyName, Double avgRating, Long totalReviewers) {
        this.propertyName = propertyName;
        this.avgRating = avgRating;
        this.totalReviewers = totalReviewers;
    }
}
