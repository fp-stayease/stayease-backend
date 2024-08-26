package com.finalproject.stayease.reviews.repository;

import com.finalproject.stayease.reviews.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserIdAndDeletedAtIsNull(Long userId);
}
