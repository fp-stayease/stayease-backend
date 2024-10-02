package com.finalproject.stayease.bookings.repository;

import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.reports.dto.properties.DailySummaryDTO;
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
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status != 'expire'")
    Page<Booking> findByUserIdAndStatusNotExpired(@Param("userId") Long userId, Pageable pageable);
    List<Booking> findByTenantId(Long tenantId, Sort sort);
    @Query("SELECT b FROM Booking b WHERE b.checkInDate = :tomorrow")
    List<Booking> findBookingsWithCheckInTomorrow(LocalDate tomorrow);
    @Query("""
        SELECT COUNT(b.id) FROM Booking b
        WHERE b.status = 'completed'
        AND b.tenant.id = :tenantId
        AND EXTRACT(YEAR FROM b.createdAt) = EXTRACT(YEAR FROM CURRENT_DATE)
        AND EXTRACT(MONTH FROM b.createdAt) = :month
    """)
    Long countCompletedBookingsByTenantId(@Param("tenantId") Long tenantId, @Param("month") int month);
    @Query("""
        SELECT COUNT(DISTINCT b.user.id) FROM Booking b
        WHERE b.tenant.id = :tenantId
        AND EXTRACT(YEAR FROM b.createdAt) = EXTRACT(YEAR FROM CURRENT_DATE)
        AND EXTRACT(MONTH FROM b.createdAt) = :month
    """)
    Long countUserBookingsByTenantId(@Param("tenantId") Long tenantId, @Param("month") int month);
    @Query("""
        SELECT b FROM Booking b
        WHERE b.tenant.id = :tenantId
        AND b.status = 'completed'
        ORDER BY b.createdAt DESC
        LIMIT 5
    """)
    List<Booking> findRecentCompletedBookingsByTenantId(@Param("tenantId") Long tenantId);

    @Query(value = """
        WITH date_series AS (
            SELECT generate_series(
                DATE_TRUNC('day', CAST(:startDate AS timestamp)),
                DATE_TRUNC('day', CAST(:endDate AS timestamp)) - INTERVAL '1 day',
                '1 day'::interval
            )::date AS date
        )
        SELECT
            TO_CHAR(ds.date, 'YYYY-MM-DD') AS date,
            COALESCE(SUM(b.total_price), 0.0) AS total_price
        FROM 
            date_series ds
        LEFT JOIN 
            bookings b ON DATE_TRUNC('day', b.created_at AT TIME ZONE 'UTC') = ds.date
            AND b.tenant_id = :tenantId
            AND b.status = 'completed'
        GROUP BY 
            ds.date
        ORDER BY 
            ds.date
    """, nativeQuery = true)
    List<Object[]> getDailySummaryForMonth(@Param("tenantId") Long tenantId,
                                           @Param("startDate") Instant startDate,
                                           @Param("endDate") Instant endDate);

}
