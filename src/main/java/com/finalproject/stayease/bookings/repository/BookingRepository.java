package com.finalproject.stayease.bookings.repository;

import com.finalproject.stayease.bookings.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status != 'EXPIRED' AND b.deletedAt IS NULL")
    Page<Booking> findByUserIdAndStatusNotExpired(@Param("userId") Long userId, Pageable pageable);
    List<Booking> findByTenantId(Long tenantId, Sort sort);
    @Query("SELECT b FROM Booking b WHERE b.checkInDate = :tomorrow AND b.deletedAt IS NULL")
    List<Booking> findBookingsWithCheckInTomorrow(LocalDate tomorrow);
    @Query("""
        SELECT COUNT(b.id) FROM Booking b
        WHERE b.status = 'PAYMENT_COMPLETE'
        AND b.tenant.id = :tenantId
        AND b.deletedAt IS NULL
        AND EXTRACT(YEAR FROM b.createdAt) = EXTRACT(YEAR FROM CURRENT_DATE)
        AND EXTRACT(MONTH FROM b.createdAt) = :month
    """)
    Double countCompletedBookingsByTenantId(@Param("tenantId") Long tenantId, @Param("month") int month);
    @Query("""
        SELECT COUNT(DISTINCT b.user.id) FROM Booking b
        WHERE b.tenant.id = :tenantId
        AND b.deletedAt IS NULL
        AND EXTRACT(YEAR FROM b.createdAt) = EXTRACT(YEAR FROM CURRENT_DATE)
        AND EXTRACT(MONTH FROM b.createdAt) = :month
    """)
    Double countUserBookingsByTenantId(@Param("tenantId") Long tenantId, @Param("month") int month);
    @Query("""
        SELECT b FROM Booking b
        WHERE b.tenant.id = :tenantId
        AND b.status = 'PAYMENT_COMPLETE'
        AND b.payment.paymentStatus = 'SETTLEMENT'
        AND b.deletedAt IS NULL
        ORDER BY b.createdAt DESC
        LIMIT 5
    """)
    List<Booking> findRecentCompletedBookingsByTenantId(@Param("tenantId") Long tenantId);
    @Query(value = """
        WITH RECURSIVE date_series AS (
            SELECT DATE_TRUNC('month', CAST(:startDate AS timestamp)) AS date
            UNION ALL
            SELECT date + INTERVAL '1 day'
            FROM date_series
            WHERE date < DATE_TRUNC('month', CAST(:endDate AS timestamp)) - INTERVAL '1 day'
        )
        SELECT
            TO_CHAR(ds.date, 'YYYY-MM-DD') AS date,
            COALESCE(SUM(b.total_price), 0.0) AS total_price
        FROM 
            date_series ds
        LEFT JOIN 
            bookings b ON DATE_TRUNC('day', b.created_at AT TIME ZONE 'UTC') = ds.date
            AND b.tenant_id = :tenantId
            AND b.status = 'PAYMENT_COMPLETE'
        LEFT JOIN
            property p ON b.property_id = p.id
        WHERE
            (:propertyId IS NULL OR p.id = :propertyId)
        GROUP BY 
            ds.date
        ORDER BY 
            ds.date
    """, nativeQuery = true)
    List<Object[]> getDailySummaryForMonth(@Param("tenantId") Long tenantId,
                                           @Param("propertyId") Long propertyId,
                                           @Param("startDate") Instant startDate,
                                           @Param("endDate") Instant endDate);
    @Query("""
        SELECT b FROM Booking b
        WHERE b.tenant.id = :tenantId
        AND b.status = 'PAYMENT_COMPLETE'
        AND b.deletedAt IS NULL
        AND EXTRACT(YEAR FROM b.createdAt) = EXTRACT(YEAR FROM CURRENT_DATE)
        AND EXTRACT(MONTH FROM b.createdAt) = :month
        AND (:propertyId IS NULL OR b.property.id = :propertyId)
    """)
    List<Booking> findCompletedPaymentBookings(
            @Param("tenantId") Long tenantId,
            @Param("propertyId") Long propertyId,
            @Param("month") int month
    );
    @Query("""
        SELECT COUNT(b.id) FROM Booking b
        WHERE b.user.id = :userId
        AND b.status = 'PAYMENT_COMPLETE'
        AND b.checkInDate >= CURRENT_DATE
        AND b.deletedAt IS NULL
    """)
    Double countUserUpcomingBookings(@Param("userId") Long userId);
    @Query("""
        SELECT COUNT(b.id) FROM Booking b
        WHERE b.user.id = :userId
        AND b.checkOutDate <= CURRENT_DATE
        AND b.status = 'PAYMENT_COMPLETE'
        AND b.deletedAt IS NULL
    """)
    Double countUserPastBookings(@Param("userId") Long userId);
    @Query("""
        SELECT b FROM Booking b
        WHERE b.status = 'PAYMENT_COMPLETE'
        AND b.checkOutDate <= CURRENT_DATE
        AND b.deletedAt IS NULL
    """)
    List<Booking> findFinishedBookings();
}
