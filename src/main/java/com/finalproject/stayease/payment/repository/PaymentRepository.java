package com.finalproject.stayease.payment.repository;

import com.finalproject.stayease.payment.entity.Payment;
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

    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = :status AND p.paymentExpirationAt < :currentTime")
    List<Payment> findByStatusAndExpirationBefore(@Param("status") String status, @Param("currentTime") Instant currentTime);
}
