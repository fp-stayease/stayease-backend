package com.finalproject.stayease.reviews.service;

import com.finalproject.stayease.reviews.entity.Review;
import com.finalproject.stayease.reviews.entity.dto.RatingDTO;
import com.finalproject.stayease.reviews.entity.dto.ReviewDTO;
import com.finalproject.stayease.reviews.entity.dto.request.UserReviewReqDTO;
import com.finalproject.stayease.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
    Review findReviewById(Long id);
    ReviewDTO addUserReview(Users user, UserReviewReqDTO reqDto, Long reviewId);
    ReviewDTO updateUserReview(Users user, UserReviewReqDTO reqDto, Long reviewId);
    void deleteUserReview(Users user, Long reviewId);
    Page<ReviewDTO> getUserReviews(Users user, String search, Pageable pageable);
    Page<ReviewDTO> getTenantReviews(Users user, String search, Pageable pageable);
    Page<ReviewDTO> getPropertiesReviews(Long propertyId, Pageable pageable);
    RatingDTO getPropertyRating(Long propertyId);
    Double getTenantRating(Users user);
    List<ReviewDTO> getAllReviews();
}