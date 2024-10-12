package com.finalproject.stayease.property.repository;

import com.finalproject.stayease.property.entity.Property;
import com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyListingDTO;
import com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO;
import com.finalproject.stayease.users.entity.Users;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

  Optional<Property> findByLocationAndDeletedAtIsNull(Point location);

  Optional<Property> findByIdAndDeletedAtIsNull(Long id);

  List<Property> findByTenantAndDeletedAtIsNull(Users tenant);

  @Query("SELECT DISTINCT p.city FROM Property p WHERE p.deletedAt IS NULL")
  List<String> findDistinctCities();

  @Query("""
      SELECT imageUrl FROM Property WHERE imageUrl IS NOT NULL AND deletedAt IS NULL
      UNION
      SELECT imageUrl FROM Room WHERE imageUrl IS NOT NULL AND deletedAt IS NULL
      """)
  List<String> findAllPropertyRoomImageUrls();

  @Query("""
      SELECT p
      FROM Property p
      JOIN FETCH p.propertyRateSetting prs
      WHERE prs.useAutoRates = true
      AND p.deletedAt IS NULL
      """)
  List<Property> findAllPropertiesWithAutoRatesEnabled();

  @Query("""
      SELECT new com.finalproject.stayease.property.entity.dto.listingDTOs.RoomPriceRateDTO(
        p.id,
        p.name,
        r.id,
        r.name,
        r.imageUrl,
        r.capacity,
        r.description,
        r.basePrice,
        psr.adjustmentType,
        psr.adjustmentRate,
        true
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
        AND ra.deletedAt IS NULL
      )
      ORDER BY r.basePrice asc
      """)
  List<RoomPriceRateDTO> findAvailableRoomRates(Long propertyId, LocalDate date);

  @Query("""
       SELECT DISTINCT p
       FROM Property p
       WHERE EXISTS (
           SELECT 1
           FROM Room r
           WHERE r.property = p
             AND NOT EXISTS (
               SELECT 1
               FROM RoomAvailability ra
               WHERE ra.room = r
                 AND :date BETWEEN ra.startDate AND ra.endDate
                 AND ra.isAvailable = false
                 AND ra.deletedAt IS NULL
           )
       )
      """)
  List<Property> findAvailablePropertiesOnDate(@Param("date") LocalDate date);

  @Query("""
            SELECT NEW com.finalproject.stayease.property.entity.dto.listingDTOs.PropertyListingDTO(
          p.id, p.tenant.tenantInfo.businessName, p.name, p.description, p.imageUrl, p.address, p.city, p.country, pc.name,
          p.longitude, p.latitude,
          (SELECT MIN(r.basePrice)
           FROM Room r
           WHERE r.property = p
           AND r.deletedAt IS NULL
          ),
          NULL
      )
      FROM Property p
      JOIN p.category pc
      JOIN p.tenant t
      JOIN t.tenantInfo ti
      WHERE p.deletedAt IS NULL
      AND (:city IS NULL OR LOWER(CAST(p.city AS string)) = LOWER(CAST(:city AS string)))
      AND (:categoryName IS NULL OR LOWER(CAST(pc.name AS string)) = LOWER(CAST(:categoryName AS string)))
      AND (:minPrice IS NULL OR
          EXISTS (
              SELECT 1
              FROM Room r
              WHERE r.property = p
              AND r.deletedAt IS NULL
              AND r.basePrice >= :minPrice
              AND NOT EXISTS (
                  SELECT 1
                  FROM RoomAvailability ra
                  WHERE ra.room = r
                  AND ra.startDate <= :startDate
                  AND ra.endDate >= :startDate
                  AND ra.isAvailable = false
                  AND ra.deletedAt IS NULL
              )
          )
      )
      AND (:maxPrice IS NULL OR\s
          EXISTS (
              SELECT 1
              FROM Room r
              WHERE r.property = p
              AND r.deletedAt IS NULL
              AND r.basePrice <= :maxPrice
              AND NOT EXISTS (
                  SELECT 1
                  FROM RoomAvailability ra
                  WHERE ra.room = r
                  AND ra.startDate <= :startDate
                  AND ra.endDate >= :startDate
                  AND ra.isAvailable = false
                  AND ra.deletedAt IS NULL
              )
          )
      )
      AND (:searchTerm IS NULL OR
          LOWER(CAST(p.name AS string)) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%')) OR
          LOWER(CAST(pc.name AS string)) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%')) OR
          LOWER(CAST(p.address AS string)) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%')) OR
          LOWER(CAST(p.city AS string)) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%')) OR
          LOWER(CAST(p.country AS string)) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%')) OR
          LOWER(CAST(ti.businessName AS string)) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%')))
      AND (:guestCount IS NULL OR
              EXISTS (
                  SELECT 1
                  FROM Room r
                  WHERE r.property = p
                  AND r.deletedAt IS NULL
                  AND r.capacity >= :guestCount
                  AND NOT EXISTS (
                      SELECT 1
                      FROM RoomAvailability ra
                      WHERE ra.room = r
                      AND ra.startDate <= :startDate
                      AND ra.endDate >= :startDate
                      AND ra.isAvailable = false
                      AND ra.deletedAt IS NULL
                  )
              )
          )
      AND EXISTS (
          SELECT 1
          FROM Room r
          WHERE r.property = p
          AND r.deletedAt IS NULL
          AND NOT EXISTS (
              SELECT 1
              FROM RoomAvailability ra
              WHERE ra.room = r
              AND ra.startDate <= :endDate
              AND ra.endDate >= :startDate
              AND ra.isAvailable = false
              AND ra.deletedAt IS NULL
          )
      )
      """)
  List<PropertyListingDTO> findAvailableProperties(
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("city") String city,
      @Param("categoryName") String categoryName,
      @Param("searchTerm") String searchTerm,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      @Param("guestCount") Integer guestCount
  );

    @Query("SELECT COUNT(p.id) FROM Property p WHERE p.tenant.id = :tenantId")
    Long countPropertiesByTenantId(@Param("tenantId") Long tenantId);

  @Modifying
  @Query("""
      DELETE FROM Property p
      WHERE p.deletedAt IS NOT NULL
      AND p.deletedAt > :timestamp
      """)
  int deleteAllDeletedPropertiesOlderThan(@Param("timestamp") Instant timestamp);
}
