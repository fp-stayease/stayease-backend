package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.PeakSeasonRate;
import com.finalproject.stayease.property.entity.Property;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PeakSeasonRateRepository extends JpaRepository<PeakSeasonRate, Long> {

  Optional<PeakSeasonRate> findByStartDateAndEndDate(LocalDate startDate, LocalDate endDate);

  @Query("""
      SELECT psr
      FROM PeakSeasonRate psr
      WHERE psr.property.id = :propertyId
      AND :date BETWEEN psr.startDate AND psr.endDate
      AND (:bookingTime BETWEEN psr.validFrom AND COALESCE(psr.endDate, :futureDate))
      ORDER BY psr.validFrom ASC
      """)
  List<PeakSeasonRate> findValidRatesByPropertyAndDate(
      @Param("propertyId") Long propertyId,
      @Param("date") LocalDate date,
      @Param("bookingTime") Instant bookingTime,
      @Param("futureDate") Instant futureDate);

  @Query("""
      SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
      FROM PeakSeasonRate p
      WHERE p.property.id = :propertyId AND p.startDate = :startDate AND p.endDate = :endDate AND p.deletedAt IS NULL
      """)
  boolean existsConflictingRate(@Param("propertyId") Long propertyId,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  List<PeakSeasonRate> findByPropertyAndEndDateAfterAndDeletedAtIsNull(Property property, LocalDate date);
}
