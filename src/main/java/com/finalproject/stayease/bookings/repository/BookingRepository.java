package com.finalproject.stayease.bookings.repository;

import com.finalproject.stayease.bookings.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status != 'expire'")
    Page<Booking> findByUserIdAndStatusNotExpired(@Param("userId") Long userId, Pageable pageable);
    List<Booking> findByTenantId(Long tenantId);
    @Query("SELECT b FROM Booking b WHERE b.checkInDate = :tomorrow")
    List<Booking> findBookingsWithCheckInTomorrow(LocalDate tomorrow);
}
