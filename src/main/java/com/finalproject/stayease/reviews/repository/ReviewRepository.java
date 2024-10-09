package com.finalproject.stayease.reviews.repository;

import com.finalproject.stayease.reviews.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("""
        SELECT r FROM Review r
        WHERE r.user.id = :userId
        AND r.deletedAt IS NULL
        AND (:search IS NULL OR CAST(r.booking.id AS string) LIKE CONCAT('%', :search, '%'))
    """)
    Page<Review> findByUserIdAndDeletedAtIsNull(@Param("userId") Long userId,
                                                @Param("search") String search,
                                                Pageable pageable);
    @Query("""
        SELECT r FROM Review r
        WHERE r.property.tenant.id = :tenantId
        AND r.isPublished = TRUE
        AND r.deletedAt IS NULL
        AND (:search IS NULL OR CAST(r.booking.id AS string) LIKE CONCAT('%', :search, '%'))
    """)
    Page<Review> findTenantReviewsAndDeletedAtIsNull(@Param("tenantId") Long tenantId,
                                                     @Param("search") String search,
                                                     Pageable pageable);
    @Query("""
        SELECT r FROM Review r
        WHERE r.property.id = :propertyId
        AND r.isPublished = TRUE
        AND r.deletedAt IS NULL
    """)
    Page<Review> findPropertiesReviewsAndDeletedAtIsNull(@Param("propertyId") Long propertyId,
                                                         Pageable pageable);
    @Query("""
        SELECT AVG(r.rating) FROM Review r
        WHERE r.property.id = :propertyId
        AND r.isPublished = TRUE
        AND r.deletedAt IS NULL
    """)
    Double calculatePropertyAverageRating(@Param("propertyId") Long propertyId);
    @Query("""
        SELECT COUNT(r.id) FROM Review r
        WHERE r.property.id = :propertyId
        AND r.isPublished = TRUE
        AND r.deletedAt IS NULL
    """)
    Long countTotalPropertiesReviewers(@Param("propertyId") Long propertyId);
}