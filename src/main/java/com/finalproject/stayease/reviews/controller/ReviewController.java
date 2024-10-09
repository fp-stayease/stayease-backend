package com.finalproject.stayease.reviews.controller;

import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.reviews.entity.dto.ReviewDTO;
import com.finalproject.stayease.reviews.entity.dto.request.UserReviewReqDTO;
import com.finalproject.stayease.reviews.service.ReviewService;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
        var response = reviewService.findReviewById(reviewId);
        return Response.successfulResponse("Review detail fetched", new ReviewDTO(response));
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
    public ResponseEntity<?> getUserReviews(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "5") int size,
                                            @RequestParam(defaultValue = "DESC") Sort.Direction direction,
                                            @RequestParam(defaultValue = "createdAt") String sortBy,
                                            @RequestParam(required = false) String search) {
        Users user = usersService.getLoggedUser();
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        var response = reviewService.getUserReviews(user, search, pageable);
        return Response.successfulResponse("User reviews fetched", response);
    }

    @GetMapping("/tenant")
    public ResponseEntity<?> getTenantReviews(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "5") int size,
                                              @RequestParam(defaultValue = "DESC") Sort.Direction direction,
                                              @RequestParam(defaultValue = "createdAt") String sortBy,
                                              @RequestParam(required = false) String search) {
        Users user = usersService.getLoggedUser();
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        var response = reviewService.getTenantReviews(user, search, pageable);
        return Response.successfulResponse("Tenant reviews fetched", response);
    }

    @GetMapping("/properties/{propertyId}")
    public ResponseEntity<?> getPropertyReviews(@PathVariable Long propertyId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "5") int size,
                                                @RequestParam(defaultValue = "createdAt") String sortBy,
                                                @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        var response = reviewService.getPropertiesReviews(propertyId, pageable);
        return Response.successfulResponse("Properties reviews fetched", response);
    }

    @GetMapping("/rating/{propertyId}")
    public ResponseEntity<?> getRatingReviews(@PathVariable Long propertyId) {
        var response = reviewService.getPropertyRating(propertyId);
        return Response.successfulResponse("Rating reviews fetched", response);
    }
}