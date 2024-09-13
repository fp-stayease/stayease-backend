package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.RoomPriceRateDTO;
import com.finalproject.stayease.users.entity.Users;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

  Optional<Property> findByLocationAndDeletedAtIsNull(Point location);
  Optional<Property> findByIdAndDeletedAtIsNull(Long id);
  List<Property> findByTenantAndDeletedAtIsNull(Users tenant);

  @Query("SELECT DISTINCT p.city FROM Property p")
  List<String> findDistinctCities();

  @Query("""
      SELECT imageUrl FROM Property WHERE imageUrl IS NOT NULL AND deletedAt IS NULL
      UNION
      SELECT imageUrl FROM Room WHERE imageUrl IS NOT NULL AND deletedAt IS NULL
      """)
  List<String> findAllPropertyRoomImageUrls();

  @Query("""
SELECT new com.finalproject.stayease.property.entity.dto.RoomPriceRateDTO(
  p.id,
  p.name,
  r.id,
  r.name,
  r.basePrice,
  psr.adjustmentType,
  psr.rateAdjustment
  )
FROM Property p
JOIN Room r ON p.id = r.property.id
LEFT JOIN PeakSeasonRate psr ON p.id = psr.property.id
AND :date BETWEEN psr.startDate AND psr.endDate
WHERE p.id = :propertyId
AND p.deletedAt IS NULL
AND r.deletedAt IS NULL
AND NOT EXISTS (
  SELECT 1
  FROM RoomAvailability ra
  WHERE ra.room.id = r.id
  AND :date BETWEEN ra.startDate AND ra.endDate
  AND ra.isAvailable = false
)
""")
  List<RoomPriceRateDTO> findAvailableRoomRates(Long propertyId, LocalDate date);
}
