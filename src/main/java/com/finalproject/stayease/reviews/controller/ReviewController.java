package com.finalproject.stayease.reviews.controller;

import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.reviews.entity.dto.request.UserReviewReqDTO;
import com.finalproject.stayease.reviews.service.ReviewService;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    private final ReviewService reviewService;
    private final UsersService usersService;

    public ReviewController(ReviewService reviewService, UsersService usersService) {
        this.reviewService = reviewService;
        this.usersService = usersService;
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewDetail(@PathVariable Long reviewId) {
        return Response.successfulResponse("Review detail fetched", reviewService.findReviewById(reviewId));
    }

    @PostMapping("/{reviewId}")
    public ResponseEntity<?> addUserReview(@RequestBody UserReviewReqDTO reqDto, @PathVariable Long reviewId) {
        Users user = usersService.getLoggedUser();
        var response = reviewService.addUserReview(user, reqDto, reviewId);

        return Response.successfulResponse(HttpStatus.CREATED.value(), "Review posted", response);
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateUserReview(@RequestBody UserReviewReqDTO reqDto, @PathVariable Long reviewId) {
        Users user = usersService.getLoggedUser();
        var response = reviewService.updateUserReview(user, reqDto, reviewId);
        return Response.successfulResponse("Review updated", response);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteUserReview(@PathVariable Long reviewId) {
        Users user = usersService.getLoggedUser();
        reviewService.deleteUserReview(user, reviewId);
        return Response.successfulResponse("Review deleted");
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserReviews() {
        Users user = usersService.getLoggedUser();
        return Response.successfulResponse("User reviews fetched", reviewService.getALLUserReviews(user));
    }

    @GetMapping("/tenant")
    public ResponseEntity<?> getTenantReviews() {
        Users user = usersService.getLoggedUser();
        return Response.successfulResponse("Tenant reviews fetched", reviewService.getTenantReviews(user));
    }

    @GetMapping("/properties/{propertyId}")
    public ResponseEntity<?> getPropertyReviews(@PathVariable Long propertyId) {
        var response = reviewService.getPropertiesReviews(propertyId);
        return Response.successfulResponse("Properties reviews fetched", response);
    }

    @GetMapping("/rating/{propertyId}")
    public ResponseEntity<?> getRatingReviews(@PathVariable Long propertyId) {
        var response = reviewService.getPropertyRating(propertyId);
        return Response.successfulResponse("Rating reviews fetched", response);
    }
}