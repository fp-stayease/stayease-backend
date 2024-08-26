package com.finalproject.stayease.reviews.service.impl;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.reviews.dto.UserReviewReqDto;
import com.finalproject.stayease.reviews.entity.Review;
import com.finalproject.stayease.reviews.repository.ReviewRepository;
import com.finalproject.stayease.reviews.service.ReviewService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Review findReviewById(Long id) {
        Optional<Review> review = reviewRepository.findById(id);
        if (review.isEmpty()) {
            throw new DataNotFoundException("Review not found");
        }
        return review.get();
    }

    @Override
    public Review createUserReviewDraft(Long userId, Long bookingId, Long propertyId) {
        Review review = new Review();
        review.setBookingId(bookingId);
        review.setUserId(userId);
        review.setPropertyId(propertyId);
        review.setPublished(false);

        return reviewRepository.save(review);
    }

    @Override
    public Review addUserReview(UserReviewReqDto reqDto, Long reviewId) {
        Review review = findReviewById(reviewId);

        review.setPublished(true);
        review.setRating(reqDto.getRating());
        review.setComment(reqDto.getComment());

        return reviewRepository.save(review);
    }

    @Override
    public Review updateUserReview(UserReviewReqDto reqDto, Long reviewId) {
        Review review = findReviewById(reviewId);

        if (reqDto.getRating() == null && review.getComment() == null) {
            throw new RuntimeException("Rating and comment are required");
        }
        if (reqDto.getRating() != null) {
            review.setRating(reqDto.getRating());
        }
        if (reqDto.getComment() != null) {
            review.setComment(reqDto.getComment());
        }

        return reviewRepository.save(review);
    }

    @Override
    public void deleteUserReview(Long reviewId) {
        Review review = findReviewById(reviewId);
        review.preRemove();

        reviewRepository.save(review);
    }

    @Override
    public List<Review> getALLUserReviews(Long userId) {
        return reviewRepository.findByUserIdAndDeletedAtIsNull(userId);
    }
}
