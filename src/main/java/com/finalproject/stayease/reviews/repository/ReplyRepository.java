package com.finalproject.stayease.reviews.repository;

import com.finalproject.stayease.reviews.entity.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {
    @Query("""
        SELECT r FROM Reply r
        WHERE r.review.id = :reviewId
        AND r.deletedAt IS NULL
    """)
    Page<Reply> findRepliesByReviewId(Long reviewId, Pageable pageable);
}