package com.finalproject.stayease.payment.repository;

import com.finalproject.stayease.payment.entity.Payment;
import com.finalproject.stayease.payment.entity.PaymentStatus;
import com.finalproject.stayease.reports.dto.overview.MonthlySalesDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(UUID bookingId);

    @Query("""
        SELECT p FROM Payment p
        WHERE p.paymentStatus = 'PENDING'
        AND p.booking.status = 'PENDING' OR p.booking.status = 'IN_PROGRESS'
        AND p.paymentExpirationAt < :currentTime
    """)
    List<Payment> findByStatusAndExpirationBefore(@Param("currentTime") Instant currentTime);

    @Query("""
        SELECT NEW com.finalproject.stayease.reports.dto.overview.MonthlySalesDTO(
            MONTH(p.createdAt),
            COALESCE(SUM(p.amount), 0)
        )
        FROM Payment p
        WHERE p.paymentStatus = 'SETTLEMENT'
        AND p.booking.tenant.id = :tenantId
        AND p.booking.status = 'PAYMENT_COMPLETE' OR p.booking.status = 'COMPLETED'
        AND p.booking.deletedAt IS NULL
        AND YEAR(p.createdAt) = :year
        GROUP BY MONTH(p.createdAt)
        ORDER BY MONTH(p.createdAt)
    """)
    List<MonthlySalesDTO> getMonthlySalesReport(@Param("tenantId") Long tenantId, @Param("year") int year);
}
