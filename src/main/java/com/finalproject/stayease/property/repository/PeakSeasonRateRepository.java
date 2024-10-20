package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.Property;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PeakSeasonRateRepository extends JpaRepository<PeakSeasonRate, Long> {

  @Query("""
      SELECT psr
      FROM PeakSeasonRate psr
      WHERE psr.property.id = :propertyId
      AND :date BETWEEN psr.startDate AND psr.endDate
      AND (:bookingTime BETWEEN psr.validFrom AND COALESCE(psr.endDate, :futureDate))
      AND psr.deletedAt IS NULL
      ORDER BY psr.validFrom ASC
      """)
  List<PeakSeasonRate> findValidRatesByPropertyAndDate(
      @Param("propertyId") Long propertyId,
      @Param("date") LocalDate date,
      @Param("bookingTime") Instant bookingTime,
      @Param("futureDate") Instant futureDate);

  @Query("""
      SELECT psr
      FROM PeakSeasonRate psr
      WHERE psr.property.id = :propertyId
      AND psr.startDate >= :startDate
      AND psr.endDate <= :endDate
      AND LOWER(psr.reason) LIKE LOWER('Automatic - %')
      AND psr.deletedAt IS NULL
      """)
  List<PeakSeasonRate> findAutomaticRatesByPropertyAndDateRange(@Param("propertyId") Long propertyId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  @Query("""
      SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
      FROM PeakSeasonRate p
      WHERE p.property.id = :propertyId AND p.startDate = :startDate AND p.endDate = :endDate AND p.deletedAt IS NULL
      """)
  boolean existsConflictingRate(@Param("propertyId") Long propertyId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  List<PeakSeasonRate> findByPropertyAndEndDateAfterAndDeletedAtIsNull(Property property, LocalDate date);

  @Modifying
  @Query("""
      DELETE FROM PeakSeasonRate psr
      WHERE psr.deletedAt < :timestamp
      """)
  int hardDeleteStaleRates(@Param("timestamp") Instant timestamp);

}
