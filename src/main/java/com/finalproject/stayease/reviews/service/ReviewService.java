package com.finalproject.stayease.reviews.service;

import com.finalproject.stayease.reviews.dto.UserReviewReqDto;
import com.finalproject.stayease.reviews.entity.Review;

import java.util.List;

public interface ReviewService {
    Review findReviewById(Long id);
    Review createUserReviewDraft(Long userId, Long bookingId, Long propertyId);
    Review addUserReview(UserReviewReqDto reqDto, Long reviewId);
    Review updateUserReview(UserReviewReqDto reqDto, Long reviewId);
    void deleteUserReview(Long reviewId);
    List<Review> getALLUserReviews(Long userId);
}
