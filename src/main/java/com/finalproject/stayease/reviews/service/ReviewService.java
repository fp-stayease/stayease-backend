package com.finalproject.stayease.reviews.service;

import com.finalproject.stayease.reviews.entity.Review;
import com.finalproject.stayease.reviews.entity.dto.RatingDTO;
import com.finalproject.stayease.reviews.entity.dto.ReviewDTO;
import com.finalproject.stayease.reviews.entity.dto.request.UserReviewReqDTO;
import com.finalproject.stayease.users.entity.Users;

import java.util.List;

public interface ReviewService {
    Review findReviewById(Long id);
    ReviewDTO addUserReview(Users user, UserReviewReqDTO reqDto, Long reviewId);
    ReviewDTO updateUserReview(Users user, UserReviewReqDTO reqDto, Long reviewId);
    void deleteUserReview(Users user, Long reviewId);
    List<ReviewDTO> getALLUserReviews(Users user);
    List<ReviewDTO> getTenantReviews(Users user);
    List<ReviewDTO> getPropertiesReviews(Long propertyId);
    RatingDTO getPropertyRating(Long propertyId);
}