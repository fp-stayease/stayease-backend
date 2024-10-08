package com.finalproject.stayease.reviews.entity.dto;

import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.reviews.entity.Review;
import com.finalproject.stayease.users.entity.Users;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewDTO {
    private Long id;
    private String comment;
    private Integer rating;
    private UsersSummary user;
    private BookingSummary booking;

    public ReviewDTO(Review review) {
        this.id = review.getId();
        this.comment = review.getComment();
        this.rating = review.getRating();
        this.user = new UsersSummary(review.getUser());
        this.booking = new BookingSummary(review.getBooking());
    }

    @Data
    static class UsersSummary {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;

        public UsersSummary(Users user) {
            this.id = user.getId();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.email = user.getEmail();
        }
    }

    static class BookingSummary {
        private UUID id;
        private String propertyName;

        public BookingSummary(Booking booking) {
            this.id = booking.getId();
            this.propertyName = booking.getProperty().getName();
        }
    }
}
