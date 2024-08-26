package com.finalproject.stayease.reviews.controller;

import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.reviews.dto.UserReviewReqDto;
import com.finalproject.stayease.reviews.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewDetail(@PathVariable Long reviewId) {
        return Response.successfulResponse("Review detail fetched", reviewService.findReviewById(reviewId));
    }

    @PostMapping("/{reviewId}")
    public ResponseEntity<?> addUserReview(@RequestBody UserReviewReqDto reqDto, @PathVariable Long reviewId) {
        return Response.successfulResponse(HttpStatus.CREATED.value(), "Review posted", reviewService.addUserReview(reqDto, reviewId));
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateUserReview(@RequestBody UserReviewReqDto reqDto, @PathVariable Long reviewId) {
        return Response.successfulResponse("Review updated", reviewService.updateUserReview(reqDto, reviewId));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteUserReview(@PathVariable Long reviewId) {
        reviewService.deleteUserReview(reviewId);
        return Response.successfulResponse("Review deleted");
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserReviews(@PathVariable Long userId) {
        return Response.successfulResponse("User reviews fetched", reviewService.getALLUserReviews(userId));
    }
}
