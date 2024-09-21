package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.PeakSeasonRate;
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
   AND :date BETWEEN psr.startDate AND COALESCE(psr.validTo, psr.endDate)
   AND (:bookingTime BETWEEN psr.validFrom AND COALESCE(psr.validTo, :futureDate))
   ORDER BY psr.validFrom ASC
   """)
  List<PeakSeasonRate> findValidRatesByPropertyAndDate(
      @Param("propertyId") Long propertyId,
      @Param("date") LocalDate date,
      @Param("bookingTime") Instant bookingTime,
      @Param("futureDate") Instant futureDate);
}
