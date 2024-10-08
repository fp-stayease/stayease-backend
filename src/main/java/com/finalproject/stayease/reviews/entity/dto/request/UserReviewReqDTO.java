package com.finalproject.stayease.reviews.entity.dto.request;

import lombok.Data;

@Data
public class UserReviewReqDTO {
    private Integer rating;
    private String comment;

    public UserReviewReqDTO(Integer rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }
}
