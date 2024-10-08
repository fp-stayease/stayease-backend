package com.finalproject.stayease.reviews.repository;

import com.finalproject.stayease.reviews.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByUserIdAndDeletedAtIsNull(Long userId);
    @Query("""
        SELECT r FROM Review r
        WHERE r.property.tenant.id = :tenantId
        AND r.isPublished IS TRUE
        AND r.deletedAt IS NULL
    """)
    List<Review> findTenantReviewsAndDeletedAtIsNull(@Param("tenantId") Long tenantId);
    @Query("""
        SELECT r FROM Review r
        WHERE r.property.id = :propertyId
        AND r.isPublished IS TRUE
        AND r.deletedAt IS NULL
    """)
    List<Review> findPropertiesReviewsAndDeletedAtIsNull(@Param("propertyId") Long propertyId);
    @Query("""
        SELECT AVG(r.rating) FROM Review r
        WHERE r.property.id = :propertyId
        AND r.isPublished IS TRUE
        AND r.deletedAt IS NULL
    """)
    Double calculatePropertyAverageRating(@Param("propertyId") Long propertyId);
}