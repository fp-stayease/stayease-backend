package com.finalproject.stayease.reviews.service.impl;

import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.bookings.service.BookingService;
import com.finalproject.stayease.exceptions.utils.DataNotFoundException;
import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.service.PropertyService;
import com.finalproject.stayease.reviews.entity.Review;
import com.finalproject.stayease.reviews.entity.dto.RatingDTO;
import com.finalproject.stayease.reviews.entity.dto.ReviewDTO;
import com.finalproject.stayease.reviews.entity.dto.request.UserReviewReqDTO;
import com.finalproject.stayease.reviews.repository.ReviewRepository;
import com.finalproject.stayease.reviews.service.ReviewService;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.TenantInfoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookingService bookingService;
    private final TenantInfoService tenantInfoService;
    private final PropertyService propertyService;

    public ReviewServiceImpl(ReviewRepository reviewRepository, BookingService bookingService, TenantInfoService tenantInfoService, PropertyService propertyService) {
        this.reviewRepository = reviewRepository;
        this.bookingService = bookingService;
        this.tenantInfoService = tenantInfoService;
        this.propertyService = propertyService;
    }

    @Override
    public Review findReviewById(Long id) {
        Optional<Review> review = reviewRepository.findById(id);
        if (review.isEmpty()) {
            throw new DataNotFoundException("Review not found");
        }
        return review.get();
    }

    @Transactional
    @Scheduled(cron = "0 0 13 * * *")
    public void createUserReviewDraft() {
        List<Booking> bookings = bookingService.findFinishedBookings();

        for (Booking booking : bookings) {
            Review review = new Review();

            review.setBooking(booking);
            review.setUser(booking.getUser());
            review.setProperty(booking.getProperty());
            review.setPublished(false);

            reviewRepository.save(review);
        }
    }

    @Override
    public ReviewDTO addUserReview(Users user, UserReviewReqDTO reqDto, Long reviewId) {
        Review review = findReviewById(reviewId);
        if (!Objects.equals(review.getUser().getId(), user.getId())) {
            throw new IllegalArgumentException("This is not your review");
        }

        review.setPublished(true);
        review.setRating(reqDto.getRating());
        review.setComment(reqDto.getComment());

        return new ReviewDTO(reviewRepository.save(review));
    }

    @Override
    public ReviewDTO updateUserReview(Users user, UserReviewReqDTO reqDto, Long reviewId) {
        Review review = findReviewById(reviewId);

        if (!Objects.equals(review.getUser().getId(), user.getId())) {
            throw new IllegalArgumentException("This is not your review");
        }

        if (reqDto.getRating() == null && review.getComment() == null) {
            throw new RuntimeException("Rating and comment are required");
        }
        if (reqDto.getRating() != null) {
            review.setRating(reqDto.getRating());
        }
        if (reqDto.getComment() != null) {
            review.setComment(reqDto.getComment());
        }

        return new ReviewDTO(reviewRepository.save(review));
    }

    @Override
    public void deleteUserReview(Users user, Long reviewId) {
        Review review = findReviewById(reviewId);
        Long userId = review.getUser().getId();
        Long tenantId = review.getProperty().getTenant().getId();

        if (!Objects.equals(user.getId(), userId) || !Objects.equals(user.getId(), tenantId)) {
            throw new IllegalArgumentException("You cannot delete this review");
        }

        review.preRemove();

        reviewRepository.save(review);
    }

    @Override
    public Page<ReviewDTO> getUserReviews(Users user, String search, Pageable pageable) {
        return reviewRepository.findByUserIdAndDeletedAtIsNull(user.getId(), search, pageable)
                .map(ReviewDTO::new);
    }

    @Override
    public Page<ReviewDTO> getTenantReviews(Users user, String search, Pageable pageable) {
        TenantInfo tenant = tenantInfoService.findTenantByUserId(user.getId());
        return reviewRepository.findTenantReviewsAndDeletedAtIsNull(user.getId(), search, pageable)
                .map(ReviewDTO::new);
    }

    @Override
    public Page<ReviewDTO> getPropertiesReviews(Long propertyId, Pageable pageable) {
        Property property = propertyService.findPropertyById(propertyId)
                .orElseThrow(() -> new DataNotFoundException("Property not found"));
        return reviewRepository.findPropertiesReviewsAndDeletedAtIsNull(property.getId(), pageable)
                .map(ReviewDTO::new);
    }

    @Override
    public RatingDTO getPropertyRating(Long propertyId) {
        Property property = propertyService.findPropertyById(propertyId)
                .orElseThrow(() -> new DataNotFoundException("Property not found"));

        Double propertyRating = reviewRepository.calculatePropertyAverageRating(property.getId());
        Long totalReviewers = reviewRepository.countTotalPropertiesReviewers(property.getId());

        return new RatingDTO(property.getName(), propertyRating, totalReviewers);
    }

    @Override
    public Double getTenantRating(Users user) {
        TenantInfo tenant = tenantInfoService.findTenantByUserId(user.getId());
        return reviewRepository.findTenantAverageRating(user.getId());
    }
}